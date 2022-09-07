package com.ihabAKH.hope.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ihabAKH.hope.R
import com.ihabAKH.hope.databinding.RequestItemBinding
import com.ihabAKH.hope.model.UserModel

class FriendRequestAdapter(
    private var context: Context,
    private var friendRequestsList: ArrayList<UserModel>
): RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding = RequestItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.request_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val requestsList = friendRequestsList[position]
        with(holder) {
            Glide.with(context).load(requestsList.avatar).circleCrop().into(binding.ivProfileImage)
            binding.tvProfileName.text = requestsList.name
        }
    }

    override fun getItemCount(): Int {
        return friendRequestsList.size
    }
}