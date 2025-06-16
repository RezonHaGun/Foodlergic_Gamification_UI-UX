package com.maranatha.foodlergic.presentation.leaderboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.maranatha.foodlergic.databinding.FragmentLeaderboardGlobalBinding
import com.maranatha.foodlergic.presentation.viewmodel.LeaderboardViewModel
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LeaderboardFragmentGlobal : Fragment() {

    // Menggunakan binding untuk mengakses view
    private var _binding: FragmentLeaderboardGlobalBinding? = null
    private val binding get() = _binding!!

    // ViewModel untuk mengambil data leaderboard
    private val viewModel: LeaderboardViewModel by viewModels()

    // Adapter untuk menampilkan data leaderboard
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var leaderboardAdapterTop3: LeaderboardAdapterTop3

    // Firebase Analytics untuk melacak event
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    // Pada onCreateView, kita akan menginisialisasi binding dan set adapter untuk RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Menggunakan ViewBinding untuk mengakses view
        _binding = FragmentLeaderboardGlobalBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Pada onViewCreated, setup RecyclerView, adapter, dan data binding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        // Inisialisasi adapter untuk RecyclerView
        leaderboardAdapterTop3 = LeaderboardAdapterTop3{ item ->
            Log.d("rezon-dbg", "on click item: $item")
            val dialog = PeopleLeaderboardDialogFragment.newInstance(item.userId,item.playerName,item.level,item.score,item.rank)
            dialog.show(parentFragmentManager, "PeopleLeaderboardDialog")
        }
        binding.leaderboardRecyclerViewTop3.layoutManager = LinearLayoutManager(requireContext())
        binding.leaderboardRecyclerViewTop3.adapter = leaderboardAdapterTop3

        leaderboardAdapter = LeaderboardAdapter { item ->
            Log.d("rezon-dbg", "on click item: $item")
            val dialog = PeopleLeaderboardDialogFragment.newInstance(item.userId,item.playerName,item.level,item.score,item.rank)
            dialog.show(parentFragmentManager, "PeopleLeaderboardDialog")
        }
        binding.leaderboardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.leaderboardRecyclerView.adapter = leaderboardAdapter

        // Memanggil fungsi untuk observe data leaderboard
        observeLeaderboard()
        viewModel.fetchLeaderboard()
    }

    // Fungsi untuk observe data leaderboard dan update RecyclerView
    private fun observeLeaderboard() {
        viewModel.leaderboardItems.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Menampilkan status loading
                    logAnalyticsEvent("loading_leaderboard", "status", "loading")
                }

                is Resource.Success -> {
                    // Pisahkan data leaderboard menjadi top 3 dan normal users
                    val topThreeUsers: MutableList<LeaderboardItem> = ArrayList()
                    val normalUsers: MutableList<LeaderboardItem> = ArrayList()

                    val leaderboardme = result.data
                    if (!leaderboardme.isNullOrEmpty()) {
                        for (i in leaderboardme.indices) {
                            if (i < 3) {
                                topThreeUsers.add(leaderboardme[i])
                            } else {
                                normalUsers.add(leaderboardme[i])
                            }
                        }
                    }
                    // Update adapter dengan data top 3 dan leaderboard biasa
                    leaderboardAdapterTop3.submitList(topThreeUsers)
                    leaderboardAdapter.submitList(normalUsers)

                    // Log event sukses
                    logAnalyticsEvent("leaderboard_success", "status", "success")
                }

                is Resource.Error -> {
                    // Menampilkan pesan error jika data gagal dimuat
                    logAnalyticsEvent("loading_leaderboard", "status", "error")
                }
            }
        }
    }

    // Fungsi untuk logging event ke Firebase Analytics
    private fun logAnalyticsEvent(eventName: String, paramKey: String, paramValue: String) {
        val bundle = Bundle().apply {
            putString(paramKey, paramValue)
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    // Pastikan binding di-nulled ketika view dihancurkan untuk menghindari memory leak
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
