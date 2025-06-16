package com.maranatha.foodlergic.presentation.reward

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.maranatha.foodlergic.databinding.DialogSingleRewardBinding
import com.maranatha.foodlergic.domain.models.Book
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingleRewardDialogFragment(
    private val Book: Book,
    private val onDismiss: () -> Unit
) : DialogFragment() {

    private var _binding: DialogSingleRewardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSingleRewardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mengatur judul reward
        binding.tvrewardTitle.text = Book.name

        // Memuat gambar ikon reward dengan Glide
        Glide.with(binding.root.context)
            .load(Book.image)
            .into(binding.ivRewardIcon)

        // Mengatur deskripsi reward
//        binding.tvrewardDescription.text = Book.summary

        // Menangani klik pada tombol download
        binding.download.setOnClickListener {
            // Implementasikan logika untuk mendownload atau membuka link
            dismiss()
            onDismiss()
        }

        // Menangani klik pada tombol close
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
