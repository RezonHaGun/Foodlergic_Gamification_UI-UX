package com.maranatha.foodlergic.presentation.allergic

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
import androidx.recyclerview.widget.GridLayoutManager
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.data.Preference
import com.maranatha.foodlergic.data.models.Allergy
import com.maranatha.foodlergic.databinding.FragmentManageAllergiesBinding
import com.maranatha.foodlergic.presentation.achievement.SingleAchievementDialogFragment
import com.maranatha.foodlergic.presentation.viewmodel.AllergyViewModel
import com.maranatha.foodlergic.presentation.viewmodel.UserProfileViewModel
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ManageAllergiesFragment : Fragment() {
    private var _binding: FragmentManageAllergiesBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferences: Preference
    private val viewModel: AllergyViewModel by viewModels()

    private val profileViewModel: UserProfileViewModel by viewModels()
    private val manageAllergiesFragmentArgs: ManageAllergiesFragmentArgs by navArgs()
    private lateinit var allergyListAdapter: AllergyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageAllergiesBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        allergyListAdapter = AllergyAdapter()
        binding.allergyRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.allergyRecyclerView.adapter = allergyListAdapter

        binding.submitAllergiesBtn.setOnClickListener {
            val selectedAllergies = allergyListAdapter.currentList.filter { it.isSelected }
            Log.d("rezon-dbg", "selectedAllergies: $selectedAllergies")

            val allergyNames = selectedAllergies.map { it.name.lowercase() }
            if (manageAllergiesFragmentArgs.isAnonymous) {
                preferences.saveAllergies(allergyNames)
                Toast.makeText(context, "Allergies saved locally.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(
                    ManageAllergiesFragmentDirections.actionManageAllergiesFragmentToPredictFragment(
                        isAnonymous = true
                    )
                )
            } else {
                viewModel.submitAllergies(allergyNames)
            }
        }
        observeSubmitAllergies()
        observeWelcomeAchievementUnlocked()
        Log.d("rezon-dbg", "isProfile: " + manageAllergiesFragmentArgs.isProfile)
        if (manageAllergiesFragmentArgs.isProfile) {
            viewModel.getUserAllergies()
            observeGetAllergiesFromAPI()
        } else {
            val defaultAllergyList = getAllergyList()

            if(preferences.getAllergies().isNotEmpty()){
                val selectedAllergies = preferences.getAllergies() // Misalnya ini list of String
                val updatedList = defaultAllergyList.map { allergy ->
                    allergy.copy(isSelected = allergy.name.toLowerCase() in selectedAllergies)
                }
                allergyListAdapter.submitList(updatedList)
            }else{
                allergyListAdapter.submitList(defaultAllergyList)
            }
        }

    }

    private fun observeGetAllergiesFromAPI() {
        viewModel.userAllergies.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Show loading state
                }

                is Resource.Success -> {
                    allergyListAdapter.submitList(result.data)
                }

                is Resource.Error -> {
                    // Show error message
                    Log.d("rezon-dbg", "error: ${result.message}")
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
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


    private fun observeSubmitAllergies() {
        viewModel.submitResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Show loading state
                }

                is Resource.Success -> {
                    if (manageAllergiesFragmentArgs.isProfile) {
                        findNavController().navigate(ManageAllergiesFragmentDirections.actionManageAllergiesFragmentToHomeFragment())
                    } else {
                        profileViewModel.checkWelcomeAchievement()
                    }
                }

                is Resource.Error -> {
                    Log.d("rezon-dbg", "error: ${result.message}")
                    // Show error message
//                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun getAllergyList(): List<Allergy> {
        return listOf(
            Allergy(name = "Fish", image = R.drawable.fish_image),
            Allergy(name = "Shell", image = R.drawable.shellfish_image),
            Allergy(name = "Crab", image = R.drawable.crab_image),
            Allergy(name = "Shrimp", image = R.drawable.shrimp_image),
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}