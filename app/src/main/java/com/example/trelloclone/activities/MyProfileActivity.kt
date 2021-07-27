package com.example.trelloclone.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.model.User
import com.example.trelloclone.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.IOException
import java.lang.Exception
import java.net.URI
import java.util.jar.Manifest

@Suppress("DEPRECATION")
class MyProfileActivity : BaseActivity() {


    private var mStorageImageUri : Uri? = null
    private lateinit var mUserDetails: User
    private var mProfileUserImage : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        FirestoreClass().loadUserData(this)

        val userImage = findViewById<ImageView>(R.id.iv_profile_user_image)
        userImage.setOnClickListener {

            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Constants.showImagePicker(this)
            }else{
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        val btnUpdate = findViewById<Button>(R.id.btn_update_my_profile)
        btnUpdate.setOnClickListener {
            showCustomProgressBar()
            if(mStorageImageUri != null){
                uploadImageOnStorage()
            }else{
                updateUserProfileData()
            }
        }
    }

    private fun setupActionBar(){
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE ){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImagePicker(this)
            }else{
                Toast.makeText(this,"Permission is required to change image",Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null){
            mStorageImageUri = data.data

            try {
                val userImage = findViewById<ImageView>(R.id.iv_profile_user_image)
                Glide
                        .with(this)
                        .load(mStorageImageUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(userImage)
            }catch(e: IOException){
                e.printStackTrace()
                Log.e("message","Failed")
                Toast.makeText(this,"Something Went Wrong Please try again later",Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun setUserDataInUi(user: User){

        mUserDetails = user

        val userImage = findViewById<ImageView>(R.id.iv_profile_user_image)
        val etName = findViewById<TextView>(R.id.et_name_my_profile)
        val etMail = findViewById<TextView>(R.id.et_email)
        val etMobile = findViewById<TextView>(R.id.et_mobile_my_profile)

        Log.e("user Image Url: ", user.image.toString())

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(userImage)

        etName.text = user.name
        etMail.text = user.email
        if(user.mobile != 0L){
            etMobile.text = user.mobile.toString()
        }
    }

    private fun uploadImageOnStorage(){

        if(mStorageImageUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_NAME" + System.currentTimeMillis() + "." + Constants.getFileExtension(this,mStorageImageUri)
            )

            sRef.putFile(mStorageImageUri!!).addOnSuccessListener {
                taskSnapshot ->
                Toast.makeText(this, "Image Saved Successfully", Toast.LENGTH_SHORT).show()
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.e("Image Uri", uri.toString())
                    mProfileUserImage = uri.toString()
                    updateUserProfileData()
                }
            }.removeOnFailureListener {
                exception ->
                hideCustomProgressDialog()
                Toast.makeText(this,"Couldn't saveImage try again later ",Toast.LENGTH_SHORT).show()
            }
        }
        hideCustomProgressDialog()
    }



    private fun updateUserProfileData(){
        val etName = findViewById<TextView>(R.id.et_name_my_profile)
        val etMobile = findViewById<TextView>(R.id.et_mobile_my_profile)


        val userHashMap = HashMap<String, Any>()

        if(mProfileUserImage!!.isNotEmpty() && mProfileUserImage != mUserDetails.image){
            Log.e("Message","Image Worked")
            userHashMap[Constants.IMAGE] = mProfileUserImage!!
        }

        if(etName.text.toString() != mUserDetails.name){
            Log.e("Message","Name Text Worked")
            userHashMap[Constants.NAME] = etName.text.toString()
        }

        if (etMobile.text.toString() != mUserDetails.mobile.toString()){
            Log.e("Message","Mobile Text Worked")
            userHashMap[Constants.MOBILE] = etMobile.text.toString().toLong()
        }

        FirestoreClass().updateUserProfileData(this@MyProfileActivity,userHashMap)

    }

    fun profileUpdateSuccess(){
        hideCustomProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }
}