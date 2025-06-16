package com.maranatha.foodlergic.presentation.viewpager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.maranatha.foodlergic.databinding.FragmentSendAddFriendBinding
import com.maranatha.foodlergic.presentation.viewmodel.FriendViewModel
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SendFragment : Fragment() {

    private var _binding: FragmentSendAddFriendBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSearch.setOnClickListener {
            val code = binding.etFriendCode.text.toString().trim()
            Log.d("FriendSearch", "Button clicked. Code: $code")
            if (code.isNotEmpty()) {
                viewModel.searchUserByFriendCode(code)
            } else {
                Toast.makeText(requireContext(), "Please enter friend code", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.friendRequestItemLayout.btnAddFriend.setOnClickListener {
            val code = binding.friendRequestItemLayout.tvFriendId.text.toString().trim()

            if (code.isNotEmpty()) {
                viewModel.sendFriendRequest(code)
            }
        }

        observeSearchUser()
        observeAddFriend()
    }

    fun observeSearchUser() {
        viewModel.searchedUser.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    Log.d("FriendSearch", "User found: ${result.data}")
                    binding.friendRequestItemLayout.tvFriendName.text = result.data?.username
                    binding.friendRequestItemLayout.tvFriendId.text = result.data?.friendCode
                    binding.friendRequestItemLayout.friendLayout.visibility = View.VISIBLE
                }

                is Resource.Error -> {
                    Log.e("FriendSearch", "Error: ${result.message}")
                    binding.friendRequestItemLayout.friendLayout.visibility = View.INVISIBLE
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun observeAddFriend() {
        viewModel.requestStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    Toast.makeText(context, "Your friend request was sent successfully!", Toast.LENGTH_LONG).show()
                }

                is Resource.Error -> {
                    Log.e("AddFriend", "Error: ${result.message}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}