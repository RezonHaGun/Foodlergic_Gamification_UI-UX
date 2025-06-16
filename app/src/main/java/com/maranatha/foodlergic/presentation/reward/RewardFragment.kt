package com.maranatha.foodlergic.presentation.reward

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.databinding.FragmentRewardBinding
import com.maranatha.foodlergic.domain.models.Book
import com.maranatha.foodlergic.presentation.viewmodel.RewardViewModel
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RewardFragment : Fragment(R.layout.fragment_reward) {

    private lateinit var binding: FragmentRewardBinding

    private lateinit var adapterStandard: RewardAdapter
    private lateinit var adapterPremium: RewardAdapter
    private lateinit var adapterExclusive: RewardAdapter

    private val rewardViewModel: RewardViewModel by viewModels()
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRewardBinding.bind(view)

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        // Setup adapters dan RecyclerView dengan layout horizontal
        adapterStandard = RewardAdapter()
        adapterPremium = RewardAdapter()
        adapterExclusive = RewardAdapter()

        binding.recyclerView.adapter = adapterStandard
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.recyclerViewPremiumBook.adapter = adapterPremium
        binding.recyclerViewPremiumBook.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.recyclerViewEksklusifBook.adapter = adapterExclusive
        binding.recyclerViewEksklusifBook.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Set onItemClick untuk masing-masing adapter
        val itemClickListener: (Book) -> Unit = { book ->
            val action = RewardFragmentDirections.actionRewardFragmentToDetailRewardFragment(book)
            findNavController().navigate(action)
            logAnalyticsEvent("reward_item_clicked", "book_title", book.name)
        }

        adapterStandard.onItemClick = itemClickListener
        adapterPremium.onItemClick = itemClickListener
        adapterExclusive.onItemClick = itemClickListener

        // Panggil fungsi untuk ambil data dan observe
        rewardViewModel.getListBook()
        observeGetAllReward()
    }

    private fun observeGetAllReward() {
        rewardViewModel.allBooks.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // TODO: tampilkan loading jika perlu
                }

                is Resource.Success -> {
                    result.data?.let { list ->
                        // Filter list berdasarkan threshold masing-masing kategori
                        val standardList = list.filter { it.threshold in 250..400 }
                        val premiumList = list.filter { it.threshold in 600..900 }
                        val exclusiveList = list.filter { it.threshold in 1100..1350 }

                        // Submit list ke adapter masing-masing
                        adapterStandard.submitList(standardList)
                        adapterPremium.submitList(premiumList)
                        adapterExclusive.submitList(exclusiveList)
                    }
                }

                is Resource.Error -> {
                    Log.d("rezon-dbg", "error: ${result.message}")
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun logAnalyticsEvent(eventName: String, paramKey: String, paramValue: String) {
        val bundle = Bundle().apply {
            putString(paramKey, paramValue)
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}
