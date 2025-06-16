package com.maranatha.foodlergic.presentation.predict

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maranatha.foodlergic.databinding.FragmentPredictResultBinding
import com.maranatha.foodlergic.presentation.achievement.MultipleAchievementBottomSheetFragment
import com.maranatha.foodlergic.presentation.achievement.SingleAchievementDialogFragment
import com.maranatha.foodlergic.presentation.viewmodel.PredictResultViewModel
import com.maranatha.foodlergic.utils.Resource
import com.maranatha.foodlergic.utils.isNetworkAvailable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PredictResultFragment : Fragment() {
    private var _binding: FragmentPredictResultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PredictResultViewModel by viewModels()
    private val args: PredictResultFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictResultBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showPredictionResult()
        observeCheckAchievement()
        binding.btnContinue.setOnClickListener {
            if (args.isAnonymous) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Create Account?")
                    .setMessage("Register to save your allergy list and track your achievements.")
                    .setPositiveButton("Register") { _, _ ->
                        findNavController().navigate(
                            PredictResultFragmentDirections.actionPredictResultFragmentToRegisterFragment(
                                true
                            )
                        )
                    }
                    .setNegativeButton("Later") { _, _ ->
                        findNavController().popBackStack()
                    }
                    .show()

            } else {
                if (!isNetworkAvailable(requireContext())) {
                    findNavController().navigate(PredictResultFragmentDirections.actionPredictResultFragmentToHomeFragment())
                } else {
                    // Cek achievement hanya jika online
                    viewModel.checkAchievements()
                }
            }
        }

    }

    private fun observeCheckAchievement() {
        viewModel.checkAchievementResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Show loading state
                }

                is Resource.Success -> {
                    if (!result.data.isNullOrEmpty()) {

                        val achievements = result.data

                        if (achievements.size == 1) {
                            SingleAchievementDialogFragment(
                                achievements.first()
                            ) {
                                // setelah achievement ditutup, navigasi ke PredictFragment
                                findNavController().navigate(PredictResultFragmentDirections.actionPredictResultFragmentToHomeFragment())
                            }.show(childFragmentManager, "SingleAchievement")
                        } else {
                            MultipleAchievementBottomSheetFragment(achievements) {
                                // setelah achievement ditutup, navigasi ke PredictFragment
                                findNavController().navigate(PredictResultFragmentDirections.actionPredictResultFragmentToHomeFragment())
                            }.show(childFragmentManager, "MultipleAchievement")
                        }
                    } else {
                        if (args.backToHome) {
                            findNavController().navigate(PredictResultFragmentDirections.actionPredictResultFragmentToHomeFragment())
                        } else {
                            findNavController().popBackStack()
                        }
                    }
                }

                is Resource.Error -> {
                    // Show error message
                    Log.d("rezon-dbg", "error: ${result.message}")
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showPredictionResult() {
        binding.tvAllergen.text = "Alergen Detected: ${args.predictedAllergen}"
        binding.tvStatus.text = if (args.isAllergic) {
            "⚠️ You have an allergy to this food. \n" +
                    "Not safe for consume"
        } else {
            "✅ You have no allergies to these foods. \n" +
                    "Safe to consume"
        }

        val imageUri = Uri.parse(args.imageUri)
        binding.imagePreview.setImageURI(imageUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}