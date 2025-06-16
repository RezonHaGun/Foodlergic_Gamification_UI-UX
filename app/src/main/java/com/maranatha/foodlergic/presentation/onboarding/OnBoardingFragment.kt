package com.maranatha.foodlergic.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.data.Preference
import com.maranatha.foodlergic.databinding.FragmentOnBoardingBinding
import com.maranatha.foodlergic.domain.models.OnBoardingItem
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingFragment : Fragment() {
    private var _binding: FragmentOnBoardingBinding? = null
    private val binding get() = _binding!!

    private lateinit var onBoardingItemAdapter: OnBoardingItemAdapter
    private lateinit var indicatorsContainer: LinearLayout

    @Inject
    lateinit var preferences: Preference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSplashScreenItems()
        setupIndicators()
        setCurrentIndicator(0)

        binding.skip.setOnClickListener {
            val action = OnBoardingFragmentDirections.actionOnBoardingFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        binding.imageview.setOnClickListener {
            val currentPosition = binding.onboardingViewPager.currentItem
            val lastPosition = onBoardingItemAdapter.itemCount - 1

            if (currentPosition < lastPosition) {
                binding.onboardingViewPager.currentItem = currentPosition + 1
            } else {
                val action = OnBoardingFragmentDirections.actionOnBoardingFragmentToLoginFragment()
                findNavController().navigate(action)
            }
        }
        binding.buttonGetStarted.setOnClickListener {
            val localAllergies = preferences.getAllergies()

            if (localAllergies.isNotEmpty()) {
                findNavController().navigate(OnBoardingFragmentDirections.actionOnBoardingFragmentToPredictFragment(isAnonymous = true))
            } else {
                val action = OnBoardingFragmentDirections.actionOnBoardingFragmentToManageAllergiesFragment(isAnonymous = true)
                findNavController().navigate(action)
            }
        }

    }

    private fun setupIndicators() {
        indicatorsContainer = binding.indicatorsContainer
        val indicators = arrayOfNulls<ImageView>(onBoardingItemAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(context)
            indicators[i]?.let {
                it.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.indicator_inactiv_bg
                    )
                )
                it.layoutParams = layoutParams
                indicatorsContainer.addView(it)
            }
        }
    }

    private fun setSplashScreenItems() {
        onBoardingItemAdapter = OnBoardingItemAdapter(
            listOf(
                OnBoardingItem(
                    onboardingImage = R.drawable.gambar1,
                    title = "Welcome Foodlergic",
                    description = "Safe Food starts with Foodlergic"
                ),
                OnBoardingItem(
                    onboardingImage = R.drawable.gambar2,
                    title = "We Are Here For You",
                    description = "make sure your food is safe from allergies"
                ),
                OnBoardingItem(
                    onboardingImage = R.drawable.gambar3,
                    title = "Ready To Start",
                    description = "Start living healthy with foodlergic"
                ),
            )
        )

        // Assuming you have a RecyclerView with ID 'onboardingViewPager'
        binding.onboardingViewPager.apply {
            adapter = onBoardingItemAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    setCurrentIndicator(position)

                    if (position == onBoardingItemAdapter.itemCount - 1) {
                        binding.buttonGetStarted.visibility = View.VISIBLE
                    } else {
                        binding.buttonGetStarted.visibility = View.GONE
                    }
                }
            })
        }
    }


    private fun setCurrentIndicator(position: Int) {
        val childCount = indicatorsContainer.childCount
        for (i in 0 until childCount) {
            val imageView = indicatorsContainer.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.indicator_bg
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.indicator_inactiv_bg
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}