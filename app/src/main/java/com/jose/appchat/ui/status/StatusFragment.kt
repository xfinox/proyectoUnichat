package com.jose.appchat.ui.status

import StatusAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.jose.appchat.R
import com.jose.appchat.model.Status
import java.util.UUID
class StatusFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var statusAdapter: StatusAdapter
    private val statusList = mutableListOf<Status>()
    private lateinit var fabUploadStatus: FloatingActionButton
    private lateinit var progressBarUpload: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_estados, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewStatus)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        statusAdapter = StatusAdapter(statusList)
        recyclerView.adapter = statusAdapter

        fabUploadStatus = view.findViewById(R.id.fabUploadStatus)
        progressBarUpload = view.findViewById(R.id.progressBarUpload)

        fabUploadStatus.setOnClickListener {
            openImagePicker()
        }

        loadStatuses()

        return view
    }

    private fun openImagePicker() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)
    }

    private fun loadStatuses() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val statusesRef = FirebaseDatabase.getInstance().reference.child("statuses").child(currentUserId)

        statusesRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                statusList.clear()
                for (statusSnapshot in snapshot.children) {
                    val status = statusSnapshot.getValue(Status::class.java)
                    status?.let { statusList.add(it) }
                }
                statusAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if necessary
            }
        })
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImageUri = data.data
            if (selectedImageUri != null) {
                uploadStatusImage(selectedImageUri)
            }
        }
    }

    private fun uploadStatusImage(imageUri: Uri) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("status_images/$currentUserId/${UUID.randomUUID()}")
        val uploadTask = storageRef.putFile(imageUri)

        progressBarUpload.visibility = View.VISIBLE

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            progressBarUpload.visibility = View.GONE
            if (task.isSuccessful) {
                val downloadUri = task.result
                val statusId = FirebaseDatabase.getInstance().reference.child("statuses").child(currentUserId).push().key ?: return@addOnCompleteListener
                val status = Status(id = statusId, imageUrl = downloadUri.toString(), timestamp = System.currentTimeMillis(), userId = currentUserId)
                FirebaseDatabase.getInstance().reference.child("statuses").child(currentUserId).child(statusId).setValue(status)
            } else {
                // Manejar error
            }
        }
    }

    companion object {
        private const val RC_SELECT_IMAGE = 2
    }
}
