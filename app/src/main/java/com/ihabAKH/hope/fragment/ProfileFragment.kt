package com.ihabAKH.hope.fragment

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ihabAKH.hope.R
import com.ihabAKH.hope.adapter.ProfilePostAdapter
import com.ihabAKH.hope.constant.Constants
import com.ihabAKH.hope.databinding.FragmentProfileBinding
import com.ihabAKH.hope.model.PostModel
import com.ihabAKH.hope.model.UserModel

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var pDialog: ProgressDialog
    private var profilePostList: ArrayList<PostModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initProgressDialog()
        initFirebaseServices()
        getProfileInfo()
        getPosts()
    }

    private fun initProgressDialog() {
        pDialog = ProgressDialog(this.context)
        pDialog.setMessage("Loading...")
        pDialog.setCancelable(false)
        pDialog.setCanceledOnTouchOutside(false)
    }

    private fun initFirebaseServices() {
        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference
    }

    private fun getProfileInfo() {
        val mCurrentUserId = mAuth.currentUser?.uid
        pDialog.show()
        mDatabase.child(Constants.FIREBASE_USERS).child(mCurrentUserId!!).get().addOnSuccessListener { userSnapShot ->
            pDialog.dismiss()
            val userInfo = userSnapShot.getValue(UserModel::class.java)
            this.context?.let { Glide.with(it).load(userInfo?.avatar).circleCrop().into(binding.ivProfileImage) }
            binding.tvProfileName.text = userInfo?.name
        }.addOnFailureListener { err ->
            pDialog.dismiss()
            this.context?.let { Toast.makeText(it, err.message, Toast.LENGTH_LONG).show() }
        }
    }

    private fun getPosts() {
        if (mAuth.currentUser == null) {
            return
        }
        pDialog.show()
        val mCurrentUser = mAuth.currentUser?.uid
        mDatabase.child(Constants.FIREBASE_POSTS).get().addOnSuccessListener { postsSnapShot ->
            if (postsSnapShot.children.count() > 0) {
                profilePostList = ArrayList()
                for (postSnapShot in postsSnapShot.children) {
                    val post = postSnapShot.getValue(PostModel::class.java)
                    if (post != null) {
                        if (post.publisher?.uid == mCurrentUser) {
                            profilePostList?.add(post)
                        }
                    }
                }
                initRecyclerView(profilePostList!!)
            }
        }.addOnFailureListener { err ->
            pDialog.dismiss()
            this.context?.let { Toast.makeText(it, err.message, Toast.LENGTH_LONG).show() }
        }
    }

    private fun initRecyclerView(posts: ArrayList<PostModel>) {
        if (posts == null) {
            return
        }
        binding.rvProfileFragment.layoutManager = LinearLayoutManager(this.context)
        val adapter = this.context?.let { ProfilePostAdapter(it, posts) }
        binding.rvProfileFragment.adapter = adapter
        pDialog.dismiss()
    }
}