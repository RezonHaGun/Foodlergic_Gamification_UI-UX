package com.maranatha.foodlergic.presentation.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.databinding.FragmentLeaderboardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private val tabTitles = listOf("Friend", "Global")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup ViewPager adapter
        binding.viewPager.adapter = LeaderboardPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val tabView = layoutInflater.inflate(R.layout.item_tab_custom, null, false)
            val title = tabView.findViewById<TextView>(R.id.tabTitle)
            title.text = tabTitles[position]
            tab.customView = tabView

            if (position == 0) {
                tabView.setBackgroundResource(R.drawable.indicator_tab_active)
                title.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.let { view ->
                    val title = view.findViewById<TextView>(R.id.tabTitle)
                    view.setBackgroundResource(R.drawable.indicator_tab_active)
                    title.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.let { view ->
                    val title = view.findViewById<TextView>(R.id.tabTitle)
                    view.setBackgroundResource(R.drawable.indicator_tab_inactive)
                    title.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
