package com.maranatha.foodlergic.presentation.register

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.data.Preference
import com.maranatha.foodlergic.databinding.FragmentRegisterBinding
import com.maranatha.foodlergic.presentation.achievement.SingleAchievementDialogFragment
import com.maranatha.foodlergic.presentation.allergic.ManageAllergiesFragmentDirections
import com.maranatha.foodlergic.presentation.viewmodel.AllergyViewModel
import com.maranatha.foodlergic.presentation.viewmodel.AuthViewModel
import com.maranatha.foodlergic.presentation.viewmodel.UserProfileViewModel
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferences: Preference

    private val viewModel: AuthViewModel by viewModels()
    private val allergyViewModel: AllergyViewModel by viewModels()
    private val profileViewModel: UserProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeRegister()
        observeSubmitAllergies()
        observeWelcomeAchievementUnlocked()
        setupHintTransparencyOnFocus(binding.passwordEditText, binding.passwordEditTextLayout)
        setupHintTransparencyOnFocus(
            binding.confirmPasswordEditText,
            binding.confirmPasswordEditTextLayout
        )


        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

            // Check if passwords match
            if (password != confirmPassword) {
                Toast.makeText(
                    context,
                    "Pastikan karakter password Anda sesuai.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // If input is valid, proceed with registration
            if (!isValidInput(name, email, password)) return@setOnClickListener

            viewModel.register(name, email, password)
        }
    }

    private fun observeWelcomeAchievementUnlocked() {
        profileViewModel.newlyUnlockedWelcome.observe(viewLifecycleOwner) { achievement ->
            if (achievement != null) {
                SingleAchievementDialogFragment(
                    achievement
                ) {
                    findNavController().navigate(ManageAllergiesFragmentDirections.actionManageAllergiesFragmentToHomeFragment())
                }.show(childFragmentManager, "SingleAchievement")
            }
        }
    }

    private fun isValidInput(name: String, email: String, password: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.nameEditText.error = getString(R.string.error_name_required)
            isValid = false
        }

        if (email.isEmpty()) {
            binding.emailEditText.error = getString(R.string.error_email_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = getString(R.string.error_invalid_email)
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = getString(R.string.error_password_required)
            isValid = false
        } else if (password.length < 6) {
            binding.passwordEditText.error = getString(R.string.error_password_length)
            isValid = false
        }

        return isValid
    }

    private fun setupHintTransparencyOnFocus(
        editText: TextInputEditText,
        textInputLayout: TextInputLayout
    ) {
        val defaultHintColor: ColorStateList? = textInputLayout.defaultHintTextColor
        val transparentColorStateList = ColorStateList.valueOf(Color.TRANSPARENT)

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Saat fokus, hint jadi transparan (seolah hilang)
                textInputLayout.setHintTextColor(transparentColorStateList)
            } else {
                // Saat kehilangan fokus, kembalikan warna hint
                if (defaultHintColor != null) {
                    textInputLayout.setHintTextColor(defaultHintColor)
                }
            }
        }
    }

    private fun observeSubmitAllergies() {
        allergyViewModel.submitResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Show loading state
                }

                is Resource.Success -> {
                    profileViewModel.checkWelcomeAchievement()
                }

                is Resource.Error -> {
                    Log.d("rezon-dbg", "error: ${result.message}")
                    // Show error message
//                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeRegister() {
        viewModel.registrationStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    updateSignupButton(isEnabled = false, text = getString(R.string.loading))
                }

                is Resource.Success -> {
                    updateSignupButton(isEnabled = true, text = getString(R.string.signup))
                    Log.d("rezon-dbg", "userId: ${result.data}")

                    val localAllergies = preferences.getAllergies()

                    if (localAllergies.isNotEmpty()) {
                        val allergyNames = localAllergies.map { it.lowercase() }
                        allergyViewModel.submitAllergies(allergyNames)
                    } else {
                        findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToManageAllergiesFragment())
                    }

                }

                is Resource.Error -> {
                    updateSignupButton(isEnabled = true, text = getString(R.string.signup))

                    Log.e("RegisterActivity", "Error: ${result.message}")
                    Toast.makeText(
                        context,
                        result.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateSignupButton(isEnabled: Boolean, text: String) {
        binding.signupButton.isEnabled = isEnabled
        binding.signupButton.text = text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
