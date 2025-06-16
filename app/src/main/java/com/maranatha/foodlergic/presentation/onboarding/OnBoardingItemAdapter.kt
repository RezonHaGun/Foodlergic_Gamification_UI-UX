package com.maranatha.foodlergic.presentation.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.domain.models.OnBoardingItem

class OnBoardingItemAdapter (private val onBoardingItem: List<OnBoardingItem>):
RecyclerView.Adapter<OnBoardingItemAdapter.SplashScreenItemViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SplashScreenItemViewHolder {
        return SplashScreenItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_splash_screen_onboarding,parent,false)
        )
    }

    override fun onBindViewHolder(holder: SplashScreenItemViewHolder, position: Int) {
        holder.bind(onBoardingItem [position])
    }

    override fun getItemCount(): Int {
        return onBoardingItem.size
    }

    inner class SplashScreenItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private  val imageOnboarding = view.findViewById<ImageView>(R.id.imageOnboarding)
        private  val textTitle = view.findViewById<TextView>(R.id.textTitle)
        private  val textDescription = view.findViewById<TextView>(R.id.textDescription)

        fun bind(onBoardingItem: OnBoardingItem) {
            imageOnboarding.setImageResource(onBoardingItem.onboardingImage)
            textTitle.text = onBoardingItem.title
            textDescription.text = onBoardingItem.description
        }
    }
}