package com.example.trelloclone.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import com.example.trelloclone.MainActivity
import com.example.trelloclone.R
import com.example.trelloclone.firebase.FirestoreClass
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )

        val typeFace: Typeface = Typeface.createFromAsset(assets,"Raleway-Bold.ttf")
        val tvAppName = findViewById<TextView>(R.id.tv_app_name)

        tvAppName.typeface = typeFace

        Handler().postDelayed(
            {

                val currentUserID = FirestoreClass().getCurrentUserID()
                if (currentUserID.isNotEmpty()) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
                }
                finish()
//                startActivity(Intent(this,IntroActivity::class.java))
//                finish()
            },2500
        )
    }
}