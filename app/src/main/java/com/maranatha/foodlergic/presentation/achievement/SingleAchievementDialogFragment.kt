package com.maranatha.foodlergic.presentation.achievement

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.maranatha.foodlergic.databinding.DialogSingleAchievementBinding
import com.maranatha.foodlergic.domain.models.Achievement
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingleAchievementDialogFragment(
    private val achievement: Achievement,
    private val onDismiss: () -> Unit
) : DialogFragment() {

    private var _binding: DialogSingleAchievementBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSingleAchievementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvAchievementTitle.text = achievement.name
        Glide.with(binding.root.context)
            .load(achievement.unlockedImage)
            .listener(object: RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("GlideError", "Image load failed", e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("Glide", "Image loaded successfully")
                    return false
                }

            })
            .into(binding.ivAchievmentIcon)
        binding.tvAchievementDescription.text = achievement.description

        binding.btnClose.setOnClickListener {
            dismiss()
            onDismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}