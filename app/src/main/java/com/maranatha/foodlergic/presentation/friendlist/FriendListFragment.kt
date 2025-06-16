package com.maranatha.foodlergic.presentation.friendlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.maranatha.foodlergic.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FriendListFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendListAdapter
    private val friendsList = mutableListOf<String>()

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_friend_list, container, false)

        recyclerView = root.findViewById(R.id.friendsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Get the current logged-in user UID
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.d("FriendListFragment", "User not logged in")
            return root
        }

        val currentUid = currentUser.uid

        // Fetch the friends data from Firestore
        db.collection("users")
            .document(currentUid)
            .collection("friends")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result!!.isEmpty) {
                        Log.d("FriendListFragment", "Tidak ada data teman di Firestore untuk user ini.")
                    } else {
                        Log.d("FriendListFragment", "Data teman ditemukan: ${task.result!!.size()} dokumen")
                        for (document in task.result!!) {
                            val friendName = document.getString("friendName") ?: "Unknown"
                            Log.d("FriendListFragment", "Teman ditemukan: $friendName")
                            friendsList.add(friendName)
                        }
                    }
                    adapter = FriendListAdapter(requireContext(), friendsList)
                    recyclerView.adapter = adapter
                } else {
                    Log.d("FriendListFragment", "Error getting documents: ", task.exception)
                }
            }

        return root
    }
}
