package com.ihabAKH.hope.fragment

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ihabAKH.hope.R
import com.ihabAKH.hope.activity.CreatePostActivity
import com.ihabAKH.hope.adapter.PostAdapter
import com.ihabAKH.hope.constant.Constants
import com.ihabAKH.hope.databinding.FragmentHomeBinding
import com.ihabAKH.hope.model.PostModel
import com.ihabAKH.hope.model.UserModel

class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var pDialog: ProgressDialog
    private var adapter: PostAdapter? = null
    private var posts: ArrayList<PostModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFirebaseServices()
        initEvents()
        getProfileImage()
        initProgressDialog()
        getPosts()
    }

    private fun initProgressDialog() {
        pDialog = ProgressDialog(this.context)
        pDialog.setMessage("Loading...")
        pDialog.setCancelable(false)
        pDialog.setCanceledOnTouchOutside(false)

    }

    private fun initEvents() {
        binding.tvPostContent.setOnClickListener(this)
    }

    private fun initFirebaseServices() {
        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference
    }

    private fun initRecyclerView(posts: ArrayList<PostModel>) {
        if (posts == null || posts.size == 0) {
            return
        }
        binding.rvHomeFragment.layoutManager = LinearLayoutManager(this.context)
        adapter = this.context?.let { PostAdapter(it, posts) }
        binding.rvHomeFragment.adapter = adapter
        pDialog.dismiss()
    }

    private fun getProfileImage() {
        if (mAuth.currentUser != null) {
            mDatabase.child(Constants.FIREBASE_USERS).child(mAuth.currentUser!!.uid).get().addOnSuccessListener {
                val user = it.getValue(UserModel::class.java)
                Glide.with(this).load(user?.avatar!!).circleCrop().into(binding.ivPostProfileImage)
            }.addOnFailureListener {
                Log.i("track", it.message!!)
            }
        }
    }

    private fun getPosts() {
        if (mAuth.currentUser == null) {
            return
        }
        pDialog.show()
        mDatabase.child(Constants.FIREBASE_POSTS).get().addOnSuccessListener {
            posts = ArrayList()
            if (it.children.count() > 0) {
                for (postSnapshot in it.children) {
                    val post = postSnapshot.getValue(PostModel::class.java)
                    if (post != null) {
                        posts!!.add(post!!)
                    }
                }
            }
            initRecyclerView(posts!!)
        }.addOnFailureListener { err ->
            pDialog.dismiss()
            this.context.let { Toast.makeText(it, err.message, Toast.LENGTH_LONG).show() }
        }
    }

    private fun goToCreateActivity() {
        startActivity(Intent(this.context, CreatePostActivity::class.java))
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvPostContent -> goToCreateActivity()
        }
    }


}