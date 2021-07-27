package com.example.trelloclone.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.trelloclone.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
open class BaseActivity : AppCompatActivity() {

    private var mDoubleBackToExit = false
    private lateinit var mProgressDialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun showCustomProgressBar(){
        mProgressDialog = Dialog(this)

        mProgressDialog.setContentView(R.layout.dialog_custom_progress)

        mProgressDialog.show()
    }

    fun hideCustomProgressDialog(){
        mProgressDialog.dismiss()
    }

    fun getCurrentUserId(): String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doubBackToExit(){
        if(mDoubleBackToExit){
            super.onBackPressed()
        }

        this.mDoubleBackToExit = true
        Toast.makeText(
            this,
            "Press again to exit",
            Toast.LENGTH_SHORT).show()

        Handler().postDelayed({mDoubleBackToExit = false},2000)
    }

    fun showErrorSnackBar(message: String){
        val snackBar = Snackbar.make(findViewById(android.R.id.content),message,Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this,R.color.snackBar_error_color))

        snackBar.show()
    }


}