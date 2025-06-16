package com.maranatha.foodlergic.presentation.viewpager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.maranatha.foodlergic.databinding.FragmentReceivedAddFriendBinding
import com.maranatha.foodlergic.domain.models.FriendRequest
import com.maranatha.foodlergic.presentation.viewmodel.FriendViewModel
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceivedFragment : Fragment() {

    private var _binding: FragmentReceivedAddFriendBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by viewModels()

    // Assuming your RecyclerView adapter will be used to display friend requests
    private lateinit var friendRequestAdapter: FriendRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceivedAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView with LinearLayoutManager and Adapter
        friendRequestAdapter = FriendRequestAdapter(object : FriendRequestAdapter.OnFriendRequestActionListener {
            override fun onAccept(request: FriendRequest) {
                viewModel.acceptFriendRequest(request)
            }

            override fun onDecline(request: FriendRequest) {
                viewModel.denyFriendRequest(request)
            }
        })
        binding.receivedFriend.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendRequestAdapter
        }

        viewModel.getReceivedFriendRequests()
        observeReceivedRequests()

        observeActionStatus()
    }

    private fun observeActionStatus(){
        viewModel.actionStatus.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Action successful", Toast.LENGTH_SHORT).show()
                    viewModel.getReceivedFriendRequests()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                    viewModel.getReceivedFriendRequests()
                }
            }
        }
    }

    private fun observeReceivedRequests() {
        viewModel.receivedRequests.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Handle loading state
                }

                is Resource.Success -> {
                    Log.d("ReceivedRequests", "Received requests: ${result.data}")
                    friendRequestAdapter.submitList(result.data)
                }

                is Resource.Error -> {
                    Log.e("ReceivedRequests", "Error: ${result.message}")
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
