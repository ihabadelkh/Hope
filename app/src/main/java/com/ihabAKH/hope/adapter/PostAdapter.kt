package com.ihabAKH.hope.adapter

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ihabAKH.hope.R
import com.ihabAKH.hope.constant.Constants
import com.ihabAKH.hope.databinding.PostItemBinding
import com.ihabAKH.hope.model.CommentModel
import com.ihabAKH.hope.model.NotificationModel
import com.ihabAKH.hope.model.PostModel
import com.ihabAKH.hope.model.UserModel
import java.util.*
import kotlin.collections.ArrayList

class PostAdapter(
    private val context: Context,
    private val posts: ArrayList<PostModel>
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private var postLikes: ArrayList<String>? = null
    private var friendRequestList: ArrayList<UserModel>? = null
    private var commentsList: ArrayList<CommentModel>? = null
    private var commentList: ArrayList<CommentModel>? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = PostItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        with(holder) {
            Glide.with(context).load(post.publisher?.avatar).circleCrop().into(binding.ivProfileImage)
            binding.tvName.text = post.publisher?.name
            binding.tvPostText.text = post.postText
            Glide.with(context).load(post.postImage).into(binding.ivPostImage)
            mAuth = Firebase.auth
            mDatabase = Firebase.database.reference
            val mCurrentUserId = mAuth.currentUser?.uid
            if (mCurrentUserId == post.publisher?.uid) {
                binding.ivAddFriend.visibility = View.GONE
            }
            mDatabase.child(Constants.FIREBASE_POSTS).child(post.uid!!).get().addOnSuccessListener {
                val postSnapshot = it.getValue(PostModel::class.java)
                if (postSnapshot?.likes != null) {
                    for (like in postSnapshot.likes!!) {
                        if (like == mCurrentUserId) {
                            Glide.with(context).load(R.drawable.active_like).into(binding.ivLike)
                            binding.ivLike.setColorFilter(ContextCompat.getColor(context, R.color.blue))
                        }
                    }
                    binding.tvNoLikes.text = "${postSnapshot.nLikes} likes"
                    binding.tvNoComments.text = if (postSnapshot.comments?.size != null) "${postSnapshot.comments?.size} comments" else "0 comments"
                }
            }

            mDatabase.child(Constants.FIREBASE_USERS).child(mCurrentUserId!!).get().addOnSuccessListener { currentUserInfo ->
                val mCurrentUserInfo = currentUserInfo.getValue(UserModel::class.java)
                mDatabase.child(Constants.FIREBASE_USERS).child(post.publisher?.uid!!).get().addOnSuccessListener { postUserInfo ->
                    val mPostUserInfo = postUserInfo.getValue(UserModel::class.java)
                    if (mPostUserInfo?.friendRequest != null) {
                        for (friendRequest in mPostUserInfo.friendRequest!!) {
                            if (friendRequest.uid == mCurrentUserInfo?.uid) {
                                binding.ivAddFriend.setColorFilter(ContextCompat.getColor(context, R.color.blue))
                            }
                        }
                    }
                }
            }

            binding.llLike.setOnClickListener {
                getLikes(post)
                Glide.with(context).load(R.drawable.active_like).into(binding.ivLike)
                binding.ivLike.setColorFilter(ContextCompat.getColor(context, R.color.blue))
                mDatabase = Firebase.database.reference
                mDatabase.child(Constants.FIREBASE_POSTS).child(post.uid!!).get().addOnSuccessListener {
                    val postInfo = it.getValue(PostModel::class.java)
                    var nLikes = postInfo?.nLikes
                    nLikes = if (nLikes != null) nLikes.plus(1) else 1
                    binding.tvNoLikes.text = "$nLikes likes"
                }
            }
            binding.ivAddFriend.setOnClickListener {
                sendFriendRequest(post)
                binding.ivAddFriend.setColorFilter(ContextCompat.getColor(context, R.color.blue))
            }
            binding.llComment.setOnClickListener {
                createComment(post)
            }
            binding.ivPostImage.setOnClickListener {
                val dialog = Dialog(context)
                dialog.setContentView(R.layout.image_layout)
                dialog.window?.setBackgroundDrawable(getDrawable(context, R.drawable.empty_bg))
                dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                val image = dialog.findViewById<ImageView>(R.id.ivImage)
                Glide.with(context).load(post.postImage).into(image)
                dialog.show()
            }

        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    private fun getLikes(post: PostModel) {
        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference
        val mCurrentUserId = mAuth.currentUser?.uid
        mDatabase.child(Constants.FIREBASE_POSTS).child(post.uid!!).get().addOnSuccessListener {
            val postSnapShot = it.getValue(PostModel::class.java)
            postLikes = ArrayList()
            if (postSnapShot?.likes == null) {
                postLikes?.add(mCurrentUserId.toString())
            }
            if (postSnapShot?.likes != null) {
                for (like in postSnapShot.likes!!) {
                    if (like != mCurrentUserId) {
                        postLikes?.add(like)
                    }
                }
                postLikes?.add(mCurrentUserId.toString())
            }
            postSnapShot?.likes = postLikes
            postSnapShot?.nLikes = postLikes?.size
            mDatabase.child(Constants.FIREBASE_POSTS).child(post.uid!!).setValue(postSnapShot)
            val notification = NotificationModel()
            notification.id = UUID.randomUUID().toString()
            notification.receiverId = postSnapShot?.publisher?.uid
            notification.notificationPostText = post.postText
            notification.notificationPostImage = post.postImage
            mDatabase.child(Constants.FIREBASE_USERS).child(mCurrentUserId!!).get().addOnSuccessListener { userSnapshot ->
                val user = userSnapshot.getValue(UserModel::class.java)
                notification.notificationProfileImage = user?.avatar
                notification.notificationMessage = user?.name + " liked your post"
                createFirebaseNotification(notification)
            }
        }

    }

    private fun sendFriendRequest(post: PostModel) {
        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference
        val mCurrentUserId = mAuth.currentUser?.uid
        if (mAuth.currentUser == null) {
            return
        }
        mDatabase.child(Constants.FIREBASE_USERS).child(mCurrentUserId!!).get().addOnSuccessListener { userInfo ->
            val currentUserInfo = userInfo.getValue(UserModel::class.java)
            friendRequestList = ArrayList()
            mDatabase.child(Constants.FIREBASE_USERS).child(post.publisher?.uid!!).get().addOnSuccessListener { postUserInfo ->
                val postUserInfo = postUserInfo.getValue(UserModel::class.java)
                if (postUserInfo?.friendRequest == null) {
                    friendRequestList?.add(currentUserInfo!!)
                }
                if (postUserInfo?.friendRequest != null) {
                    for (friendRequest in postUserInfo.friendRequest!!) {
                        if (friendRequest != currentUserInfo) {
                            friendRequestList?.add(friendRequest)
                        }
                    }
                    friendRequestList?.add(currentUserInfo!!)
                }
                postUserInfo?.friendRequest = friendRequestList
                mDatabase.child(Constants.FIREBASE_USERS).child(post.publisher?.uid!!).setValue(postUserInfo)
                val notification = NotificationModel()
                notification.id = UUID.randomUUID().toString()
                notification.receiverId = postUserInfo?.uid
                mDatabase.child(Constants.FIREBASE_USERS).child(mCurrentUserId!!).get().addOnSuccessListener { userSnapshot ->
                    val user = userSnapshot.getValue(UserModel::class.java)
                    notification.notificationProfileImage = user?.avatar
                    notification.notificationMessage = user?.name + " sent you a friend request"
                    createFirebaseNotification(notification)
                }
            }
        }
    }

    private fun createFirebaseNotification(notification: NotificationModel) {
        if (notification.id == null) {
            return
        }
        mDatabase = Firebase.database.reference
        mDatabase.child(Constants.FIREBASE_NOTIFICATIONS).child(notification.id!!).setValue(notification)
    }

    private fun createComment(post: PostModel) {
        val dialog = Dialog(context)
        mDatabase = Firebase.database.reference
        mAuth = Firebase.auth
        val mCurrentUserId = mAuth.currentUser?.uid
        val comments = CommentModel()
        dialog.setContentView(R.layout.comment_layout)
        dialog.window?.setBackgroundDrawable(getDrawable(context, R.drawable.round_corners))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val comment = dialog.findViewById<EditText>(R.id.edtComment)
        val send = dialog.findViewById<ImageView>(R.id.ivSend)
        send.setOnClickListener {
            comments.whoCommented = mCurrentUserId.toString()
            comments.comment = comment.text.toString()
            mDatabase.child(Constants.FIREBASE_POSTS).child(post.uid!!).get().addOnSuccessListener {
                val postInfo = it.getValue(PostModel::class.java)
                commentsList = ArrayList()
                if (comment != null && postInfo?.comments == null) {
                    commentsList?.add(comments)
                }
                if (comment != null && postInfo?.comments != null) {
                    for (commentInComments in postInfo.comments!!) {
                        commentsList?.add(commentInComments)
                    }
                    commentsList?.add(comments)
                }
                comment.text.clear()
                postInfo?.comments = commentsList
                mDatabase.child(Constants.FIREBASE_POSTS).child(post.uid!!).setValue(postInfo)
            }

        }
        mDatabase.child(Constants.FIREBASE_POSTS).child(post.uid!!).get().addOnSuccessListener {
            val postInfo = it.getValue(PostModel::class.java)
            if (postInfo?.comments != null) {
                commentList = ArrayList()
                for (commentInComments in postInfo.comments!!) {
                    commentList?.add(commentInComments)
                }
                val rvComments = dialog.findViewById<RecyclerView>(R.id.rvComments)
                rvComments.layoutManager = LinearLayoutManager(context)
                val adapter = CommentAdapter(context, commentList!!)
                rvComments.adapter = adapter
            }
        }
        dialog.show()
    }



}