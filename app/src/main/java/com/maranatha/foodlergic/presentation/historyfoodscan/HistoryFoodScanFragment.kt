package com.maranatha.foodlergic.presentation.historyfoodscan

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.maranatha.foodlergic.databinding.FragmentHistoryFoodScanBinding
import com.maranatha.foodlergic.presentation.profile.FoodScanHistoryAdapter
import com.maranatha.foodlergic.presentation.viewmodel.UserProfileViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFoodScanFragment : Fragment() {

    private var _binding: FragmentHistoryFoodScanBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: UserProfileViewModel by viewModels()
    private lateinit var foodScanHistoryAdapter: FoodScanHistoryAdapter
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryFoodScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        profileViewModel.fetchAllPredictions()
        foodScanHistoryAdapter = FoodScanHistoryAdapter()
        binding.historyFoodScanRecyclerview.adapter = foodScanHistoryAdapter
        binding.historyFoodScanRecyclerview.layoutManager = LinearLayoutManager(context)

        loadFoodScanHistory()

        // Log event for viewing food scan history
        logAnalyticsEvent("view_food_scan_history", "action", "viewed")
    }

    private fun loadFoodScanHistory() {
        profileViewModel.allPredictions.observe(viewLifecycleOwner) { listFoodScan ->
            Log.d("rezon-dbg", "loadFoodScanHistory: ${listFoodScan.toString()}")
            foodScanHistoryAdapter.submitList(listFoodScan)

            // Log event when food scan history is loaded
            logAnalyticsEvent("food_scan_history_loaded", "item_count", listFoodScan.size.toString())
        }
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
