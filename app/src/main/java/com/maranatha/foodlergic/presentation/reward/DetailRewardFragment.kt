package com.maranatha.foodlergic.Reward

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.databinding.FragmentDetailRewardBinding
import com.maranatha.foodlergic.domain.models.Book
import com.maranatha.foodlergic.presentation.viewmodel.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailRewardFragment : Fragment(R.layout.fragment_detail_reward) {

    private lateinit var binding: FragmentDetailRewardBinding
    private val profileViewModel: UserProfileViewModel by viewModels()
    private val args: DetailRewardFragmentArgs by navArgs()

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailRewardBinding.bind(view)

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        // Get the book object passed from the RewardFragment
        val book = args.book
        binding.detailReward.text = book.name

        Glide.with(requireContext())
            .load(book.image)  // Menggunakan URL gambar jika tersedia
            .into(binding.detailImage)
        binding.summaryText.text = book.summary

        // Log event when viewing book details
        logAnalyticsEvent("view_reward_details", "book_name", book.name)

        // Load user profile data
        loadUserProfile()
        profileViewModel.getUserData()

        // Handle download button click
        binding.downloadButton.setOnClickListener {
            openGoogleDriveInBrowser(book)
            // Log event when user clicks on download button
            logAnalyticsEvent("download_button_clicked", "book_name", book.name)
        }
    }

    // Function to open Google Drive link in a browser
    private fun openGoogleDriveInBrowser(book: Book) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(book.urlBook))
        startActivity(intent)
    }

    // Function to load user profile and update progress and button status
    private fun loadUserProfile() {
        profileViewModel.userData.observe(viewLifecycleOwner) { user ->
            val book = args.book
            val point = user.point
            val maxScan = book.threshold

            val progress = point.toFloat() / maxScan.toFloat()  // Calculate progress

            // Update progress bar and reward text
            binding.levelProgressBar.max = 100
            binding.levelProgressBar.progress = (progress * 100).toInt().coerceAtMost(100)

            // Update rewardProgress to reflect the user's progress towards the book's threshold
            binding.rewardProgress.text =
                "$point / $maxScan Points"  // Show user points vs threshold

            // Enable or disable download button based on progress
            binding.downloadButton.isEnabled =
                progress >= 1.0 // Enable only if the user has enough points
        }
    }

    // Log event to Firebase Analytics
    private fun logAnalyticsEvent(eventName: String, paramKey: String, paramValue: String) {
        val bundle = Bundle().apply {
            putString(paramKey, paramValue)
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}
