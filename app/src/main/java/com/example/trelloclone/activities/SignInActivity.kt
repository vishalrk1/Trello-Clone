package com.example.trelloclone.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.trelloclone.MainActivity
import com.example.trelloclone.R
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.model.User
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        auth = FirebaseAuth.getInstance()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )

        val btnSignIn = findViewById<Button>(R.id.btn_sign_in)
        btnSignIn.setOnClickListener {
            signInRegisteredUser()
        }

        setupActionBar()
    }

    private fun setupActionBar(){
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_sign_in_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_icon_24dp)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun signInSucess(user: User){
        hideCustomProgressDialog()
        Log.e(" LoggedIn userData","$user")
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    private fun signInRegisteredUser(){
        val eMail: String = findViewById<TextView>(R.id.et_email_in).text.toString().trim { it <= ' ' }
        val password: String = findViewById<TextView>(R.id.et_password_in).text.toString().trim { it <= ' ' }

        if(validateUser(eMail,password)){
            showCustomProgressBar()
            auth.signInWithEmailAndPassword(eMail, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Sign in", "signInWithEmail:success")
                        val user = auth.currentUser
                        FirestoreClass().loadUserData(this)

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign in", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        hideCustomProgressDialog()
                        finish()
                    }
                }
        }
    }

    private fun validateUser(eMail: String,password: String): Boolean{
        return when{
            TextUtils.isEmpty(eMail) -> {
                showErrorSnackBar("Please Enter a Name")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please Enter a Name")
                false
            }else -> {
                true
            }
        }
    }
}