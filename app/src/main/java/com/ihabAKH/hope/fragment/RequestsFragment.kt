package com.ihabAKH.hope.fragment

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ihabAKH.hope.R
import com.ihabAKH.hope.adapter.FriendRequestAdapter
import com.ihabAKH.hope.constant.Constants
import com.ihabAKH.hope.databinding.FragmentRequestsBinding
import com.ihabAKH.hope.model.UserModel

class RequestsFragment : Fragment() {

    private lateinit var binding: FragmentRequestsBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private var friendRequestsList: ArrayList<UserModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFirebaseServices()
        getFriendRequests()
    }

    private fun initFirebaseServices() {
        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference
    }

    private fun getFriendRequests() {
        if (mAuth.currentUser == null) {
            return
        }
        val mCurrentUserId = mAuth.currentUser?.uid
        mDatabase.child(Constants.FIREBASE_USERS).child(mCurrentUserId!!).get().addOnSuccessListener { dataSnapshot ->
            val currentUserInfo = dataSnapshot.getValue(UserModel::class.java)
            if (currentUserInfo?.friendRequest != null) {
                friendRequestsList = ArrayList()
                for (request in currentUserInfo.friendRequest!!) {
                    friendRequestsList?.add(request)
                }
                initRecyclerView(friendRequestsList!!)
            }
        }.addOnFailureListener { err ->
            this.context?.let { Toast.makeText(it, err.message, Toast.LENGTH_LONG).show() }
        }
    }

    private fun initRecyclerView(friendRequestsList: ArrayList<UserModel>) {
        if (friendRequestsList == null) {
            return
        }
        binding.rvFriendRequests.layoutManager = LinearLayoutManager(this.context)
        val adapter = this.context?.let { FriendRequestAdapter(it, friendRequestsList) }
        binding.rvFriendRequests.adapter = adapter
    }

}