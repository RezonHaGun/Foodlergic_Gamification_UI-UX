package com.maranatha.foodlergic.presentation.login

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
import com.maranatha.foodlergic.databinding.FragmentLoginBinding
import com.maranatha.foodlergic.presentation.onboarding.OnBoardingFragmentDirections
import com.maranatha.foodlergic.presentation.viewmodel.AuthViewModel
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var preferences: Preference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLoginResult()
        setupClickListeners()

        setupHintTransparencyOnFocus(binding.passwordEditText, binding.passwordEditTextLayout)

    }

    private fun setupClickListeners() {
        binding.tvRegisterNow.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        binding.tvTryAsGuest.setOnClickListener {
            val localAllergies = preferences.getAllergies()

            if (localAllergies.isNotEmpty()) {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToPredictFragment(isAnonymous = true))
            } else {
                val action = LoginFragmentDirections.actionLoginFragmentToManageAllergiesFragment(isAnonymous = true)
                findNavController().navigate(action)
            }
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (!validateInput(email, password)) {
                Toast.makeText(context, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

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

    private fun observeLoginResult() {
        viewModel.loginStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    updateLoginButton(false, getString(R.string.loading))
                }

                is Resource.Success -> {
                    updateLoginButton(true, getString(R.string.login))

                    Toast.makeText(context, getString(R.string.login_success), Toast.LENGTH_SHORT)
                        .show()
                    Log.d("rezon-dbg", "userId: ${result.data}")

                    val user = result.data
                    if (user != null) {
                        if (user.isAllergySubmitted) {
                            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
                        } else {
                            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToManageAllergiesFragment())
                        }
                    }
                }

                is Resource.Error -> {
                    updateLoginButton(true, getString(R.string.login))

                    Log.e("LoginActivity", "Error: ${result.message}")
                    Toast.makeText(context, getString(R.string.login_failed), Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }
    }

    private fun updateLoginButton(isEnabled: Boolean, text: String) {
        binding.loginButton.isEnabled = isEnabled
        binding.loginButton.text = text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}