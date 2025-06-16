package com.maranatha.foodlergic.presentation.leaderboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.databinding.DialogPeopleLeaderboardBinding
import com.maranatha.foodlergic.presentation.achievement.AchievementAdapter
import com.maranatha.foodlergic.presentation.viewmodel.UserProfileViewModel
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PeopleLeaderboardDialogFragment : DialogFragment() {
    private var _binding: DialogPeopleLeaderboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var achievementAdapter: AchievementAdapter
    private val viewModel: UserProfileViewModel by viewModels()
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPeopleLeaderboardBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Ambil data userId dari arguments
        val userId = arguments?.getString("userId")
        val name = arguments?.getString("name")
        val level = arguments?.getString("level")
        val score = arguments?.getInt("score")
        val rank = arguments?.getInt("rank")

        viewModel.fetchAndCombineFriendAchievements(userId.orEmpty())
        binding.achievementRecyclerView.layoutManager = GridLayoutManager(context, 3)
        achievementAdapter = AchievementAdapter()
        binding.achievementRecyclerView.adapter = achievementAdapter
        binding.namepeople.levelTextView.text = level
        binding.namepeople.scoreTextView.text = score.toString()
        binding.namepeople.playerNameTextView.text = name
        binding.namepeople.rankTextView.text = rank.toString()+"."


        Log.d("PeopleLeaderboard", "Dialog dibuka untuk userId: $userId")
        // Anda bisa menggunakan userId untuk menampilkan data terkait pengguna tersebut
        observeFriendAchievement()
        val closeButton: Button = view.findViewById(R.id.btnClose)
        closeButton.setOnClickListener {
            dismiss() // Menutup dialog
        }
    }

    private fun observeFriendAchievement() {
        viewModel.combineFriendAchievements.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Tampilkan loading
                }

                is Resource.Success -> {
                    val achievements = result.data ?: emptyList()
                    // Filter hanya achievement yang sudah di-unlock saja
                    Log.d("rezon-dbg", "Achievement $achievements")
                    val unlockedAchievements = achievements.filter { it.isUnlocked }
                    Log.d("rezon-dbg", "observe unlocked achievements: $unlockedAchievements")
                    achievementAdapter.submitList(unlockedAchievements)
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
    companion object {
        fun newInstance(userId: String,name: String, level: String,score: Int,rank: Int): PeopleLeaderboardDialogFragment {
            val fragment = PeopleLeaderboardDialogFragment()
            val args = Bundle()
            args.putString("userId", userId)
            args.putString("name", name)
            args.putString("level", level)
            args.putInt("score", score)
            args.putInt("rank", rank)
            fragment.arguments = args
            return fragment
        }
    }

}




