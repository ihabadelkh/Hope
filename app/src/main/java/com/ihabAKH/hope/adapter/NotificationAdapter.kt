package com.ihabAKH.hope.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ihabAKH.hope.R
import com.ihabAKH.hope.databinding.NotificationItemBinding
import com.ihabAKH.hope.model.NotificationModel

class NotificationAdapter(
    private var context: Context,
    private var notifications: ArrayList<NotificationModel>
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = NotificationItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notifications = notifications[position]
        with(holder) {
            Glide.with(context).load(notifications.notificationProfileImage).circleCrop().into(binding.notificationProfileImage)
            binding.notificationMessage.text = notifications.notificationMessage
            binding.notificationPostText.text = notifications.notificationPostText
            Glide.with(context).load(notifications.notificationPostImage).into(binding.notificationPostImage)
        }
    }

    override fun getItemCount(): Int {
        return notifications.size
    }
}