package com.maranatha.foodlergic.presentation.addfriend

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
import com.maranatha.foodlergic.databinding.FragmentAddFriendBinding
import com.maranatha.foodlergic.presentation.viewpager.ViewPagerAddFriendAdapter

class AddFriendFragment : Fragment() {

    private var _binding: FragmentAddFriendBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ViewPagerAddFriendAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->

            val tabView = LayoutInflater.from(context).inflate(R.layout.item_tab_custom, null)
            val title = tabView.findViewById<TextView>(R.id.tabTitle)

            title.text = if (position == 0) "Received" else "Sent"
            tab.customView = tabView

            // Kasih aktif ke tab pertama
            if (position == 0) {
                tabView.setBackgroundResource(R.drawable.indicator_tab_active)
                title.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {

                    val view = tab.customView ?: return
                    val title = view.findViewById<TextView>(R.id.tabTitle)
                    view.setBackgroundResource(R.drawable.indicator_tab_active)
                    title.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {

                    val view = tab.customView ?: return
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
