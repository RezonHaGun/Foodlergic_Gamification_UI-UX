package com.maranatha.foodlergic.presentation.achievement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.maranatha.foodlergic.databinding.FragmentMultipleAchievementBinding
import com.maranatha.foodlergic.domain.models.Achievement

class MultipleAchievementBottomSheetFragment(
    private val achievements: List<Achievement>,
    private val onDismiss: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentMultipleAchievementBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultipleAchievementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView with adapter
        binding.recyclerViewAchievements.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewAchievements.adapter = MultipleAchievementAdapter(achievements)

        // Optionally, dismiss bottom sheet after a delay or on button click
        binding.btnClose.setOnClickListener {
            dismiss()
            onDismiss()
        }
    }
}
