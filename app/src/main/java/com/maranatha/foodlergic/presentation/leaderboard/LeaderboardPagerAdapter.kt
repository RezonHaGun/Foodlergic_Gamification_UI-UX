package com.maranatha.foodlergic.presentation.leaderboard

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LeaderboardPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2  // Dua tab: Friend & Global

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LeaderboardFragmentFriend()  // Tab pertama (Friend)
            1 -> LeaderboardFragmentGlobal()  // Tab kedua (Global)
            else -> throw IllegalStateException("Invalid position")
        }
    }
}
