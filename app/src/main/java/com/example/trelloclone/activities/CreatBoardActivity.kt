package com.example.trelloclone.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.model.Board
import com.example.trelloclone.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

@Suppress("DEPRECATION")
class CreatBoardActivity : BaseActivity() {

    private var mStorageImageUri : Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creat_board)

        setupActionBar()

        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        val boardImage = findViewById<ImageView>(R.id.iv_board_image)
        boardImage.setOnClickListener {
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

        val btnCreate = findViewById<Button>(R.id.btn_create)
        btnCreate.setOnClickListener {
            if(mStorageImageUri != null){
                uploadBoardImage()
            }else{
                showCustomProgressBar()
                createBoard()
            }
        }
    }

    private fun setupActionBar(){
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_create_board_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "Create Board"
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
                Toast.makeText(this,"Permission is required to change image", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null){
            mStorageImageUri = data.data

            try {
                val boardImage = findViewById<ImageView>(R.id.iv_board_image)
                Glide
                        .with(this)
                        .load(mStorageImageUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(boardImage)
            }catch(e: IOException){
                e.printStackTrace()
                Log.e("message","Failed")
                Toast.makeText(this,"Something Went Wrong Please try again later", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createBoard(){

        val etBoardName = findViewById<TextView>(R.id.et_board_name)

        val assignedUsers: ArrayList<String> = ArrayList()
        assignedUsers.add(getCurrentUserId())

        val board = Board(
                etBoardName.text.toString(),
                mBoardImageURL,
                mUserName,
                assignedUsers,
        )

        FirestoreClass().createBoard(this,board)

    }

    fun boardCreatedSuccessfully(){
        hideCustomProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun uploadBoardImage(){
        showCustomProgressBar()

        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "BOARD_NAME" + System.currentTimeMillis() + "." + Constants.getFileExtension(this,mStorageImageUri)
        )

        sRef.putFile(mStorageImageUri!!).addOnSuccessListener {
            taskSnapshot ->
            Toast.makeText(this, "Image Saved Successfully", Toast.LENGTH_SHORT).show()
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                uri ->
                Log.e("Board Uri", uri.toString())
                mBoardImageURL = uri.toString()

                createBoard()
            }
        }.removeOnFailureListener {
            exception ->
            hideCustomProgressDialog()
            Toast.makeText(this,"Couldn't saveImage try again later ",Toast.LENGTH_SHORT).show()
        }
    }


}