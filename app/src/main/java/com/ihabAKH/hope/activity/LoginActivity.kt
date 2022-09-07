package com.ihabAKH.hope.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ihabAKH.hope.R
import com.ihabAKH.hope.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var pDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initEvents()
        initProgressDialog()
        initFirebaseAuth()
    }

    private fun initEvents() {
        binding.btnCreateNewAccount.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
    }

    private fun initProgressDialog() {
        pDialog = ProgressDialog(this)
        pDialog.setMessage("Loading...")
        pDialog.setCanceledOnTouchOutside(false)
        pDialog.setCancelable(false)
    }

    private fun goToSignupActivity() {
        startActivity(Intent(this, SignupActivity::class.java))
        finish()
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun initFirebaseAuth() {
        mAuth = Firebase.auth
    }

    private fun loginWithFirebase(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            pDialog.dismiss()
            goToMainActivity()
        }.addOnFailureListener {
            pDialog.dismiss()
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun validateCredentials(email: String, password: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            binding.tiEmail.error = "Email is required"
            return false
        }
        if (TextUtils.isEmpty(password)) {
            binding.tiPassword.error = "Password is required"
            return false
        }
        return true
    }

    private fun login() {
        val email = binding.tiEmail.editText?.text.toString()
        val password = binding.tiPassword.editText?.text.toString()
        if (validateCredentials(email, password)) {
            pDialog.show()
            loginWithFirebase(email, password)
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null) {
            goToMainActivity()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnCreateNewAccount -> goToSignupActivity()
            R.id.btnLogin -> login()
        }
    }
}