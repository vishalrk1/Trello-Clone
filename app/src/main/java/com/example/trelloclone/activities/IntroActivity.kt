package com.example.trelloclone.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import com.example.trelloclone.R

@Suppress("DEPRECATION")
class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )

        val btnSignUp = findViewById<Button>(R.id.btn_sign_up_intro)
        val btnSignIn = findViewById<Button>(R.id.btn_sign_in_intro)

        btnSignUp.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
        }

        btnSignIn.setOnClickListener {
            startActivity(Intent(this,SignInActivity::class.java))
        }
    }


}