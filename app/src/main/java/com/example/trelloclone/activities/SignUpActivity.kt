package com.example.trelloclone.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.trelloclone.R
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Suppress("DEPRECATION")
class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )

        setupActionBar()

    }

    private fun setupActionBar(){
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_sign_up_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_icon_24dp)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val btnSignUp = findViewById<Button>(R.id.btn_sign_up)
        btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    fun userRegisteredSucess(){
        Log.e("Login","registered Successful")
        Toast.makeText(this,"registered Successful",Toast.LENGTH_SHORT).show()
        hideCustomProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun registerUser(){
        val name: String = findViewById<TextView>(R.id.et_name).text.toString().trim {it <= ' '}
        val eMail: String = findViewById<TextView>(R.id.et_email).text.toString().trim { it <= ' ' }
        val password: String = findViewById<TextView>(R.id.et_password).text.toString().trim { it <= ' ' }

        if(validateForm(name,eMail,password)){
            showCustomProgressBar()
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(eMail,password).addOnCompleteListener {
                task ->
                if (task.isSuccessful){
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email
                    val user = User(firebaseUser.uid,name,eMail)
                    Log.e("Sign up","$name is registered with $registeredEmail ==> $user")
                    FirestoreClass().registerUser(this,user)
                }else {
                    Log.e("Sign Up","$name is not registered")
                    Toast.makeText(this,"$name is not registered ",Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }


    }

    private fun validateForm(name: String,eMail: String,passoward: String): Boolean{
        return when{
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please Enter a Name")
                false
            }
            TextUtils.isEmpty(eMail) -> {
                showErrorSnackBar("Please Enter a Name")
                false
            }
            TextUtils.isEmpty(passoward) -> {
                showErrorSnackBar("Please Enter a Name")
                false
            }else -> {
                true
            }
        }
    }
}