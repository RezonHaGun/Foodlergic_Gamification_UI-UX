package com.maranatha.foodlergic.presentation.achievement

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.maranatha.foodlergic.databinding.FragmentAchievementBinding
import com.maranatha.foodlergic.presentation.viewmodel.UserProfileViewModel
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AchievementFragment : Fragment() {
    private var _binding: FragmentAchievementBinding? = null
    private val binding get() = _binding!!

    private lateinit var achievementAdapter: AchievementAdapter
    private val profileViewModel: UserProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Memanggil fungsi untuk mendapatkan pencapaian dari Firestore
        profileViewModel.fetchAndCombineAchievements()

        binding.allergyRecyclerView.layoutManager = GridLayoutManager(context, 2)
        achievementAdapter = AchievementAdapter()
        binding.allergyRecyclerView.adapter = achievementAdapter

        observeUserAchievement()
    }

    private fun observeUserAchievement() {
        profileViewModel.combineUserAchievements.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Tampilkan loading
                }

                is Resource.Success -> {
                    val achievements = result.data ?: emptyList()
                    achievementAdapter.submitList(achievements)
                }

                is Resource.Error -> {
                    Log.d("rezon-dbg", "Error: ${result.message}")
                    // Tampilkan pesan error
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
