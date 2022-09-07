package com.ihabAKH.hope.fragment

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ihabAKH.hope.R
import com.ihabAKH.hope.adapter.NotificationAdapter
import com.ihabAKH.hope.constant.Constants
import com.ihabAKH.hope.databinding.FragmentNotificaionBinding
import com.ihabAKH.hope.model.NotificationModel

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificaionBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var pDialog: ProgressDialog
    private var notificationsList: ArrayList<NotificationModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNotificaionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initProgressDialog()
        initFirebaseServices()
        getNotifications()
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

    private fun getNotifications() {
        if (mAuth.currentUser == null) {
            return
        }
        val mCurrentUserId = mAuth.currentUser?.uid
        pDialog.show()
        mDatabase.child(Constants.FIREBASE_NOTIFICATIONS).get().addOnSuccessListener {
            if (it.children.count() > 0) {
                notificationsList = ArrayList()
                for (notification in it.children) {
                    val notifications = notification.getValue(NotificationModel::class.java)
                    if (notifications != null && notifications.receiverId.equals(mCurrentUserId)) {
                        notificationsList?.add(notifications)
                    }
                }
            }
            initRecyclerView(notificationsList!!)

        }.addOnFailureListener { err ->
            pDialog.dismiss()
            this.context.let { Toast.makeText(it, err.message, Toast.LENGTH_LONG).show() }
        }
    }

    private fun initRecyclerView(notifications: ArrayList<NotificationModel>) {
        binding.rvNotifications.layoutManager = LinearLayoutManager(this.context)
        val adapter = this.context?.let { NotificationAdapter(it, notifications) }
        binding.rvNotifications.adapter = adapter
        pDialog.dismiss()
    }

}