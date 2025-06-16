package com.maranatha.foodlergic.presentation.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.maranatha.foodlergic.databinding.FragmentHomeBinding
import com.maranatha.foodlergic.domain.models.LevelThreshold
import com.maranatha.foodlergic.presentation.achievement.MultipleAchievementBottomSheetFragment
import com.maranatha.foodlergic.presentation.achievement.SingleAchievementDialogFragment
import com.maranatha.foodlergic.presentation.predict.PredictResultFragmentDirections
import com.maranatha.foodlergic.presentation.profile.FoodScanHistoryAdapter
import com.maranatha.foodlergic.presentation.reward.MultipleRewardBottomSheetFragment
import com.maranatha.foodlergic.presentation.reward.SingleRewardDialogFragment
import com.maranatha.foodlergic.presentation.viewmodel.RewardViewModel
import com.maranatha.foodlergic.presentation.viewmodel.UserProfileViewModel
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var foodScanHistoryAdapter: FoodScanHistoryAdapter
    private lateinit var warningFoodAdapter: WarningFoodAdapter
    private lateinit var bookAdapter: BookAdapter

    private val profileViewModel: UserProfileViewModel by viewModels()

    private val rewardViewModel: RewardViewModel by viewModels()

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        profileViewModel.getUserWithLevel()
        profileViewModel.fetchRecentPredictions()
        profileViewModel.loadFoodWarnings()

        bookAdapter = BookAdapter()
        binding.bookhorizontalview.adapter = bookAdapter
        binding.bookhorizontalview.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        foodScanHistoryAdapter = FoodScanHistoryAdapter()
        binding.rvFoodScanHistory.adapter = foodScanHistoryAdapter
        binding.rvFoodScanHistory.layoutManager = LinearLayoutManager(context)

        warningFoodAdapter = WarningFoodAdapter()
        binding.rvFoodWarnings.adapter = warningFoodAdapter
        binding.rvFoodWarnings.layoutManager = LinearLayoutManager(context)

        rewardViewModel.getListBook()
        rewardViewModel.checkRewards()

        loadUserProfile()
        loadFoodScanHistory()
        observeGetAllReward()
        observeAllergyFoodRisk()
        observeCheckAchievement()

        // Log navigation to HistoryFoodScanFragment
        binding.foodScanViewAll.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHistoryFoodScanFragment())
            logAnalyticsEvent("navigate_to_history_food_scan", "action", "clicked")
        }

        // Log navigation to RewardFragment
        binding.rewardViewAll.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToRewardFragment())
            logAnalyticsEvent("navigate_to_reward", "action", "clicked")
        }
    }

    private fun observeCheckAchievement() {
        rewardViewModel.checkRewardsResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Show loading state
                }

                is Resource.Success -> {
                    if (!result.data.isNullOrEmpty()) {

                        val rewards = result.data

                        if (rewards.size == 1) {
                            SingleRewardDialogFragment(
                                rewards.first()
                            ) {
                            }.show(childFragmentManager, "SingleAchievement")
                        } else {
                            MultipleRewardBottomSheetFragment(rewards) {
                            }.show(childFragmentManager, "MultipleAchievement")
                        }
                    }
                }

                is Resource.Error -> {
                    // Show error message
                    Log.d("rezon-dbg", "error: ${result.message}")
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeGetAllReward() {
        rewardViewModel.allBooks.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Show loading state
                }

                is Resource.Success -> {
                    bookAdapter.submitList(result.data)
                }

                is Resource.Error -> {
                    // Show error message
                    Log.d("rezon-dbg", "error: ${result.message}")
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeAllergyFoodRisk() {
        profileViewModel.warningFoods.observe(viewLifecycleOwner) { listWarning ->
            if (listWarning.isNotEmpty()) {
                binding.warningLayout.visibility = View.VISIBLE
                warningFoodAdapter.submitList(listWarning)
            } else {
                binding.warningLayout.visibility = View.GONE
            }
        }
    }

    private fun loadUserProfile() {
        profileViewModel.userDataWithLevel.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Error -> {

                }

                is Resource.Loading -> {}
                is Resource.Success -> {
                    val userWithLevel = resource.data
                    binding.helloText.text = "Hello, ${userWithLevel?.user?.username}"

                    val point = userWithLevel?.user?.point
                    point?.let {
                        val progress = getLevelProgress(point, userWithLevel.levelInfo)
                        binding.levelProgressBar.max = 100
                        binding.levelProgressBar.progress = (progress * 100).toInt()

                        binding.rewardProgress.text = "$point / ${userWithLevel.levelInfo.maxScanCount} Points"
                        binding.tvLevelTitle.text = userWithLevel.levelInfo.name

                        binding.tvAllergyInfo.text = "You have allergies to: ${userWithLevel.allergies.joinToString(", ") }"
                        // Log level progression event
                        logAnalyticsEvent("user_level_progress", "level",
                            userWithLevel.levelInfo.name
                        )
                    }
                }
            }
        }
    }

    private fun loadFoodScanHistory() {
        profileViewModel.recentPredictions.observe(viewLifecycleOwner) { listFoodScan ->
            Log.d("rezon-dbg", "loadFoodScanHistory: ${listFoodScan.toString()}")
            foodScanHistoryAdapter.submitList(listFoodScan)

            // Log event for loading food scan history
            logAnalyticsEvent(
                "food_scan_history_loaded",
                "item_count",
                listFoodScan.size.toString()
            )
        }
    }

//    private fun getLevelInfo(scanCount: Int): LevelInfo {
//        return when {
//            scanCount < 100 -> LevelInfo("Rookie", 0, 100)
//            scanCount < 1000 -> LevelInfo("Beginner", 100, 1000)
//            scanCount < 30000 -> LevelInfo("Explorer", 1000, 30000)
//            scanCount < 60000 -> LevelInfo("Expert", 30000, 60000)
//            else -> LevelInfo("Master Scanner", 60000, 90000)
//        }
//    }

    private fun getLevelProgress(scanCount: Int, levelInfo: LevelThreshold): Float {
        val clamped = (scanCount - levelInfo.minScanCount).coerceAtLeast(0)
        val total = (levelInfo.maxScanCount - levelInfo.minScanCount).toFloat()
        return (clamped / total).coerceIn(0f, 1f)
    }

    // Log Firebase Analytics event
    private fun logAnalyticsEvent(eventName: String, paramKey: String, paramValue: String) {
        val bundle = Bundle().apply {
            putString(paramKey, paramValue)
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
