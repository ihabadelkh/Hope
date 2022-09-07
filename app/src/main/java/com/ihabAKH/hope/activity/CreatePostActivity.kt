package com.ihabAKH.hope.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import com.google.android.gms.common.internal.Objects
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.ihabAKH.hope.R
import com.ihabAKH.hope.constant.Constants
import com.ihabAKH.hope.databinding.ActivityCreatePostBinding
import com.ihabAKH.hope.model.PostModel
import com.ihabAKH.hope.model.UserModel
import java.io.ByteArrayOutputStream
import java.util.*

class CreatePostActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mStorage: StorageReference
    private lateinit var pDialog: ProgressDialog
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpToolbar()
        initFirebaseServices()
        initEvents()
        initProgressDialog()

    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.tbCreateActivity)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.tbCreateActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun initFirebaseServices() {
        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference
        mStorage = Firebase.storage.reference
    }

    private fun initEvents() {
        binding.llAddImage.setOnClickListener(this)
        binding.btnPost.setOnClickListener(this)
    }

    private fun initProgressDialog() {
        pDialog = ProgressDialog(this)
        pDialog.setMessage("Loading...")
        pDialog.setCancelable(false)
        pDialog.setCanceledOnTouchOutside(false)
    }

    private fun chooseImage() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            val uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI
            intent.setDataAndType(uri, "image/*")
            intent.putExtra("crop", "true")
            startActivityForResult(intent, Constants.PICK_IMAGE_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            val uri = data?.data
            imageUri = uri!!
            Log.i("track", imageUri.toString())
            if (uri != null) {
                val bitmapImage = uriToBitmap(uri)
                Glide.with(this).load(bitmapImage).into(binding.ivPostImage)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage()
                }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun createFirebasePost(uid: String, postText: String, postImage: String) {
        if (mAuth.currentUser != null) {
            mDatabase.child(Constants.FIREBASE_USERS).child(mAuth.currentUser!!.uid).get()
                .addOnSuccessListener { userInfo ->
                    val user = userInfo.getValue(UserModel::class.java)
                    val post = PostModel()
                    post.uid = uid
                    post.postText = postText
                    post.postImage = postImage
                    post.publisher = user
                    mDatabase.child(Constants.FIREBASE_POSTS).child(uid).setValue(post)
                    user?.nPosts = if (user?.nPosts != null) user?.nPosts!!.plus(1) else 1
                    mDatabase.child(Constants.FIREBASE_USERS).child(mAuth.currentUser!!.uid)
                        .setValue(user)
                    pDialog.dismiss()
                    goToMainActivity()

                }.addOnFailureListener {
                    pDialog.dismiss()
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }

        }
    }

    private fun getUploadedImage(): ByteArray {
        binding.ivPostImage.isDrawingCacheEnabled = true
        binding.ivPostImage.buildDrawingCache()
        val bitmap = (binding.ivPostImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    private fun uploadPost() {
        pDialog.show()
        val uuid = UUID.randomUUID().toString()
        val postRef = mStorage.child("posts/$uuid.jpg")
        postRef.putBytes(getUploadedImage()).addOnSuccessListener {
            postRef.downloadUrl.addOnSuccessListener {
                createFirebasePost(uuid, binding.edtPostText.text.toString(), it.toString())
            }
        }.addOnFailureListener {
            pDialog.dismiss()
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.llAddImage -> chooseImage()
            R.id.btnPost -> uploadPost()
        }
    }
}