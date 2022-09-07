package com.ihabAKH.hope.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.ihabAKH.hope.R
import com.ihabAKH.hope.constant.Constants.Companion.FIREBASE_USERS
import com.ihabAKH.hope.constant.Constants.Companion.PICK_IMAGE_REQUEST_CODE
import com.ihabAKH.hope.constant.Constants.Companion.READ_EXTERNAL_STORAGE_REQUEST_CODE
import com.ihabAKH.hope.databinding.ActivitySignupBinding
import com.ihabAKH.hope.model.UserModel
import java.io.ByteArrayOutputStream
import java.util.*

class SignupActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mStorage: StorageReference
    private lateinit var pDialog: ProgressDialog
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initEvents()
        initFirebaseServices()
        initProgressDialog()
    }

    private fun initProgressDialog() {
        pDialog = ProgressDialog(this)
        pDialog.setMessage("Loading...")
        pDialog.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
        binding.profileImage.setOnClickListener(this)
        binding.btnSignup.setOnClickListener(this)
        binding.btnHaveAccount.setOnClickListener(this)
    }

    private fun initFirebaseServices() {
        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference
        mStorage = Firebase.storage.reference
    }

    @SuppressLint("IntentReset")
    private fun chooseImage() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            intent.type = "image/*"
            intent.putExtra("crop", "true")
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                return
            }
            val uri = data?.data
            imageUri = uri
            if (uri != null) {
                val bitmapImage = uriToBitmap(uri)
                Glide.with(this).load(bitmapImage).circleCrop().into(binding.profileImage)
                binding.tvSelectImage.visibility = View.GONE
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }

    private fun validate(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (TextUtils.isEmpty(name.trim())) {
            binding.tiName.error = "Name is required"
            return false
        } else {
            binding.tiName.isErrorEnabled = false
        }
        if (TextUtils.isEmpty(email.trim())) {
            binding.tiEmail.error = "Email is required"
            return false
        } else {
            binding.tiEmail.isErrorEnabled = false
        }
        if (TextUtils.isEmpty(password.trim())) {
            binding.tiPassword.error = "Password is required"
            return false
        } else {
            binding.tiPassword.isErrorEnabled = false
        }
        if (password.length < 6) {
            binding.tiPassword.error = "Password is too short"
        }
        if (TextUtils.isEmpty(confirmPassword.trim())) {
            binding.tiConfirmPassword.error = "You have to confirm password"
            return false
        } else {
            binding.tiConfirmPassword.isErrorEnabled = false
        }
        if (confirmPassword != password) {
            binding.tiConfirmPassword.error = "confirm password not equal password"
            return false
        } else {
            binding.tiConfirmPassword.isErrorEnabled = false
        }
        return true
    }

    private fun register() {
        val name = binding.tiName.editText?.text?.toString()?.trim()
        val email = binding.tiEmail.editText?.text?.toString()?.trim()
        val password = binding.tiPassword.editText?.text?.toString()?.trim()
        val confirmPassword = binding.tiConfirmPassword.editText?.text?.toString()?.trim()
        if (validate(name!!, email!!, password!!, confirmPassword!!)) {
            pDialog.show()
            uploadUserAvatarToFirebase(name!!, email!!, password!!)
        }
    }

    private fun uploadUserAvatarToFirebase(name: String, email: String, password: String) {
        val uuid = UUID.randomUUID()
        val avatarRef = mStorage.child("users/$uuid.jpg")
        binding.profileImage.isDrawingCacheEnabled = true
        binding.profileImage.buildDrawingCache()
        val bitmap = (binding.profileImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = avatarRef.putBytes(data)

        uploadTask.addOnFailureListener {
            pDialog.dismiss()
            Toast.makeText(this, "Cannot upload your avatar", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            avatarRef.downloadUrl.addOnSuccessListener { uri ->
                if (uri != null) {
                    createFirebaseAccount(name, email, password, uri.toString())
                }
            }
        }


    }

    private fun createFirebaseAccount(
        name: String,
        email: String,
        password: String,
        avatar: String
    ) {
        if (email != null && password != null) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                pDialog.dismiss()
                val userId = mAuth.currentUser?.uid
                insertFirebaseDatabase(userId.toString(), name, email, avatar)

            }.addOnFailureListener {
                pDialog.dismiss()
                Toast.makeText(
                    this@SignupActivity,
                    it.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                this@SignupActivity,
                "Please provide your email and password",
                Toast.LENGTH_LONG
            ).show();
        }


    }

    private fun insertFirebaseDatabase(
        userId: String,
        name: String,
        email: String,
        avatar: String
    ) {
        val userModel = UserModel()
        userModel.uid = userId
        userModel.name = name
        userModel.email = email
        userModel.avatar = avatar
        mDatabase.child(FIREBASE_USERS).child(userId).setValue(userModel)
        goToLoginActivity()
    }

    private fun goToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.profileImage -> chooseImage()
            R.id.btnSignup -> register()
            R.id.btnHaveAccount -> goToLoginActivity()
        }
    }
}