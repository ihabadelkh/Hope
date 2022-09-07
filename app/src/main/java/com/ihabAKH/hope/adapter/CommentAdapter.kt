package com.ihabAKH.hope.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ihabAKH.hope.R
import com.ihabAKH.hope.constant.Constants
import com.ihabAKH.hope.databinding.CommentItemBinding
import com.ihabAKH.hope.model.CommentModel
import com.ihabAKH.hope.model.UserModel

class CommentAdapter(
    private var context: Context,
    private var commentList: ArrayList<CommentModel>
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = CommentItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentList[position]
        mDatabase = Firebase.database.reference
        with(holder) {
            mDatabase.child(Constants.FIREBASE_USERS).child(comment.whoCommented!!).get().addOnSuccessListener {
                val userInfo = it.getValue(UserModel::class.java)
                Glide.with(context).load(userInfo?.avatar).circleCrop().into(binding.ivCommentProfileImage)
                binding.tvCommentProfileName.text = userInfo?.name
                binding.tvComment.text = comment.comment
            }
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }
}