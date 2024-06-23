package com.jose.appchat.ui.status

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.jose.appchat.R
import com.jose.appchat.model.State
import java.io.ByteArrayOutputStream
import java.util.*

class AddStatusActivity : AppCompatActivity() {

    private val RC_SELECT_IMAGE = 2
    private lateinit var selectedImageBytes: ByteArray
    private lateinit var imageViewSelected: ImageView
    private lateinit var buttonUpload: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_status)

        imageViewSelected = findViewById(R.id.imageViewSelected)
        buttonUpload = findViewById(R.id.buttonUpload)

        imageViewSelected.setOnClickListener {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }
            startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)
        }

        buttonUpload.setOnClickListener {
            if (::selectedImageBytes.isInitialized) {
                uploadImageToFirebase()
            } else {
                Toast.makeText(this, "Por favor, seleccione una imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(contentResolver, selectedImagePath)

            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectedImageBytes = outputStream.toByteArray()

            imageViewSelected.setImageBitmap(selectedImageBmp)
        }
    }

    private fun uploadImageToFirebase() {
        val ref = FirebaseStorage.getInstance().reference.child("status_images/${UUID.randomUUID()}")
        ref.putBytes(selectedImageBytes)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveStatusToDatabase(uri.toString())
                }
            }
    }

    private fun saveStatusToDatabase(imageUrl: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userId = user.uid
        val username = user.displayName ?: "Unknown User"
        val timestamp = System.currentTimeMillis()

        val status = State(userId, username, imageUrl, timestamp)
        val ref = FirebaseDatabase.getInstance().getReference("/states/$userId").push()
        ref.setValue(status)
            .addOnSuccessListener {
                finish()
            }
    }
}
