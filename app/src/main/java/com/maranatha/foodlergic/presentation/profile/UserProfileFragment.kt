package com.maranatha.foodlergic.presentation.profile

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.databinding.FragmentUserProfileBinding
import com.maranatha.foodlergic.presentation.viewmodel.AllergyViewModel
import com.maranatha.foodlergic.presentation.viewmodel.UserProfileViewModel
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AllergyViewModel by viewModels()
    private val profileViewModel: UserProfileViewModel by viewModels()

    private lateinit var achievementAdapter: UserProfileAchievementAdapter
    private lateinit var allergyAdapter: UserProfileAllergicAdapter

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val firestore = FirebaseFirestore.getInstance()
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserProfile()
        loadFriendCount()

        profileViewModel.getUserData()
        profileViewModel.getAchievement()
        viewModel.getUserAllergies()

        achievementAdapter = UserProfileAchievementAdapter()
        allergyAdapter = UserProfileAllergicAdapter()

        val spacing = resources.getDimensionPixelSize(R.dimen.spacing) // Bisa gunakan dimensi yang sudah ada

        binding.rvAchievments.layoutManager = GridLayoutManager(context, 3)
        binding.rvAchievments.adapter = achievementAdapter

        binding.selectedAllergiesRecyclerView.layoutManager = GridLayoutManager(context, 4)
        binding.selectedAllergiesRecyclerView.adapter = allergyAdapter

        observeUserAchievement()
        observeGetAllergiesFromAPI()
        observeCLearUserSession()

        binding.tvAchievementEditText.setOnClickListener {
            findNavController().navigate(UserProfileFragmentDirections.actionUserProfileFragmentToAchievementFragment())
            logAnalyticsEvent("navigate_achievement", "action", "clicked")
        }
        binding.viewMyalergies.setOnClickListener {
            findNavController().navigate(
                UserProfileFragmentDirections.actionUserProfileFragmentToManageAllergiesFragment(
                    isProfile = true
                )
            )
            logAnalyticsEvent("navigate_manage_allergies", "action", "clicked")
        }
        binding.addfriendbtn.setOnClickListener {
            findNavController().navigate(UserProfileFragmentDirections.actionUserProfileFragmentToAddFriendFragment())
            logAnalyticsEvent("navigate_add_friend", "action", "clicked")
        }
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    profileViewModel.clearSession()
                    logAnalyticsEvent("logout", "status", "clicked")
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
        }
        binding.btnShare.setOnClickListener {
            shareFriendCode()
            logAnalyticsEvent("navigate_share_friend_code", "action", "clicked")
        }

        binding.tvFriendCount.setOnClickListener {
            findNavController().navigate(R.id.action_userProfileFragment_to_friendListFragment)
        }
    }

    private fun loadFriendCount() {
        if (userId.isBlank()) {
            binding.tvFriendCount.text = "0 Friends"
            Log.e("UserProfileFragment", "User ID is empty, cannot load friends")
            return
        }

        firestore.collection("users")
            .document(userId)
            .collection("friends")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val friendCount = querySnapshot.size()
                val friendText = if (friendCount == 1) "$friendCount Friend" else "$friendCount Friends"
                binding.tvFriendCount.text = friendText
                logAnalyticsEvent("friend_count_loaded", "count", friendCount.toString())
            }
            .addOnFailureListener { e ->
                binding.tvFriendCount.text = "0 Friends"
                Log.e("UserProfileFragment", "Failed to load friend count", e)
                logAnalyticsEvent("friend_count_load_error", "error", e.message ?: "unknown error")
            }
    }

    private fun observeGetAllergiesFromAPI() {
        viewModel.userAllergies.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    logAnalyticsEvent("loading_allergies", "status", "loading")
                }
                is Resource.Success -> {
                    val userAllergic = result.data?.filter { it.isSelected }
                    allergyAdapter.submitList(userAllergic)
                    logAnalyticsEvent("allergies_fetched", "status", "success")
                }
                is Resource.Error -> {
                    Log.d("UserProfileFragment", "error: ${result.message}")
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    logAnalyticsEvent("allergies_fetched", "status", "error")
                }
            }
        }
    }

    private fun observeCLearUserSession() {
        profileViewModel.clearUserStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Loading state
                }
                is Resource.Success -> {
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    logAnalyticsEvent("user_logout_success", "status", "success")
                    findNavController().navigate(UserProfileFragmentDirections.actionUserProfileFragmentToLoginFragment())
                }
                is Resource.Error -> {
                    Log.d("UserProfileFragment", "error: ${result.message}")
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    logAnalyticsEvent("user_logout_error", "status", "error")
                }
            }
        }
    }

    private fun observeUserAchievement() {
        profileViewModel.userAchievements.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Loading state
                }

                is Resource.Success -> {
                    val achievements = result.data ?: emptyList()
                    Log.d("UserProfileFragment", "achievements: $achievements")
                    // Filter hanya yang isUnlocked == true
                    val unlockedAchievements = achievements.filter { it.isUnlocked }
                    Log.d("UserProfileFragment", "Unlocked achievements (filtered): $unlockedAchievements")
                    // Ambil maksimal 3 item pertama yang unlocked
                   val top3Unlocked = unlockedAchievements.take(3)

                    Log.d(
                        "UserProfileFragment",
                        "Filtered top 3 unlocked achievements: $top3Unlocked"
                    )
                    achievementAdapter.submitList(top3Unlocked)
                    logAnalyticsEvent("achievements_fetched", "status", "success")
                }

                is Resource.Error -> {
                    Log.d("UserProfileFragment", "error: ${result.message}")
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    logAnalyticsEvent("achievements_fetched", "status", "error")
                }
            }
        }
    }

    private fun loadUserProfile() {
        profileViewModel.userData.observe(viewLifecycleOwner) { user ->
            binding.tvUsername.text = user.username
            binding.tvLevelTitle.text = user.level
            binding.tvFriendCode.text = user.friendCode
            logAnalyticsEvent("user_profile_loaded", "status", "success")
        }
    }

    private fun shareFriendCode() {
        val code = binding.tvFriendCode.text.toString()
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Add me on Foodlergic! My Friend Code: $code")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share with"))
    }

    private fun logAnalyticsEvent(eventName: String, paramKey: String, paramValue: String) {
        val bundle = Bundle().apply {
            putString(paramKey, paramValue)
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user_profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                profileViewModel.clearSession()
                logAnalyticsEvent("logout", "status", "clicked")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
