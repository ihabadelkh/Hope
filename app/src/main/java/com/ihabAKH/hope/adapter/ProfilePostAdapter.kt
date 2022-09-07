package com.ihabAKH.hope.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ihabAKH.hope.R
import com.ihabAKH.hope.databinding.PostItemBinding
import com.ihabAKH.hope.model.PostModel

class ProfilePostAdapter(
    private var context: Context,
    private var postsList: ArrayList<PostModel>
): RecyclerView.Adapter<ProfilePostAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding = PostItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postList = postsList[position]
        with(holder) {
            Glide.with(context).load(postList.postImage).into(binding.ivPostImage)
            Glide.with(context).load(postList.publisher?.avatar).circleCrop().into(binding.ivProfileImage)
            binding.tvName.text = postList.publisher?.name
            binding.tvPostText.text = postList.postText
            binding.tvNoLikes.text = "${postList.nLikes} likes"
            val mAuth = Firebase.auth
            val mCurrentUserId = mAuth.currentUser?.uid
            if (mCurrentUserId == postList.publisher?.uid) {
                binding.ivAddFriend.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return postsList.size
    }
}