package com.maranatha.foodlergic.presentation.viewpager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAddFriendAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // Jumlah total fragment/tab
    override fun getItemCount(): Int = 2

    // Mengembalikan fragment berdasarkan posisi tab
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ReceivedFragment()  // Fragment untuk "Received"
            1 -> SendFragment()      // Fragment untuk "Sent"
            else -> throw IllegalStateException("Invalid position")
        }
    }
}
