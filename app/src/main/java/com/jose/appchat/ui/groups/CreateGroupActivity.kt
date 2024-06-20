package com.jose.appchat.ui.groups

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.jose.appchat.R
import com.jose.appchat.model.Contact_group
import com.jose.appchat.model.Group
import com.squareup.picasso.Picasso
import java.util.UUID

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var editTextGroupName: EditText
    private lateinit var btnCreateGroup: Button
    private lateinit var imageViewGroup: ImageView
    private lateinit var recyclerViewContacts: RecyclerView
    private lateinit var contactsAdapter: ContactsAdapter
    private val contactsList = mutableListOf<Contact_group>()
    private val contactsWithProfilePictures = mutableListOf<Pair<Contact_group, String?>>()
    private val selectedContacts = mutableListOf<Contact_group>()

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        editTextGroupName = findViewById(R.id.editTextGroupName)
        btnCreateGroup = findViewById(R.id.btnCreateGroup)
        imageViewGroup = findViewById(R.id.imageViewGroup)
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts)

        recyclerViewContacts.layoutManager = LinearLayoutManager(this)
        contactsAdapter = ContactsAdapter(contactsWithProfilePictures) { contact, isSelected ->
            if (isSelected) {
                selectedContacts.add(contact)
            } else {
                selectedContacts.remove(contact)
            }
        }
        recyclerViewContacts.adapter = contactsAdapter

        loadContacts()

        btnCreateGroup.setOnClickListener {
            checkGroupNameAndCreate()
        }

        imageViewGroup.setOnClickListener {
            selectGroupImage()
        }
    }

    private fun loadContacts() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val contactsRef = FirebaseDatabase.getInstance().reference.child("contacts").child(currentUserId)
        val usersRef = FirebaseDatabase.getInstance().reference.child("users")

        contactsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contactsList.clear()
                contactsWithProfilePictures.clear()
                for (contactSnapshot in snapshot.children) {
                    val contact = contactSnapshot.getValue(Contact_group::class.java)
                    contact?.let { contactsList.add(it) }
                }
                loadProfilePictures(usersRef)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error si es necesario
            }
        })
    }

    private fun loadProfilePictures(usersRef: DatabaseReference) {
        val storageReference = FirebaseStorage.getInstance().reference

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (contact in contactsList) {
                    val userSnapshot = snapshot.child(contact.userId)
                    val profilePicturePath = userSnapshot.child("profilePicturePath").getValue(String::class.java)
                    if (profilePicturePath != null) {
                        val storageRefPath = "gs://unichatfb.appspot.com$profilePicturePath"
                        storageReference.child(profilePicturePath).downloadUrl.addOnSuccessListener { uri ->
                            val completeProfilePicturePath = uri.toString()
                            Log.d("CreateGroupActivity", "User: ${contact.nombre}, Profile Picture URL: $completeProfilePicturePath")
                            contactsWithProfilePictures.add(Pair(contact, completeProfilePicturePath))
                            contactsAdapter.notifyDataSetChanged()
                        }.addOnFailureListener {
                            Log.e("CreateGroupActivity", "Error getting download URL for $storageRefPath", it)
                            contactsWithProfilePictures.add(Pair(contact, null))
                            contactsAdapter.notifyDataSetChanged()
                        }
                    } else {
                        contactsWithProfilePictures.add(Pair(contact, null))
                        contactsAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error si es necesario
            }
        })
    }

    private fun selectGroupImage() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Group Image"), RC_SELECT_IMAGE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            Picasso.get().load(selectedImageUri).into(imageViewGroup)
        }
    }

    private fun checkGroupNameAndCreate() {
        val groupName = editTextGroupName.text.toString().trim()
        if (groupName.isEmpty()) {
            editTextGroupName.error = "El nombre del grupo es obligatorio"
            return
        }

        val groupsRef = FirebaseDatabase.getInstance().reference.child("groups")
        groupsRef.orderByChild("name").equalTo(groupName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(this@CreateGroupActivity, "Ya existe un grupo con ese nombre", Toast.LENGTH_SHORT).show()
                } else {
                    if (selectedImageUri != null) {
                        uploadGroupImageAndCreateGroup(groupName)
                    } else {
                        createGroup(groupName, "")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error si es necesario
            }
        })
    }

    private fun uploadGroupImageAndCreateGroup(groupName: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val groupId = FirebaseDatabase.getInstance().reference.child("groups").push().key ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("group_images/$groupId/${UUID.randomUUID()}")

        selectedImageUri?.let { uri ->
            storageRef.putFile(uri).addOnCompleteListener { uploadTask ->
                if (uploadTask.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        createGroup(groupName, downloadUri.toString())
                    }
                } else {
                    Toast.makeText(this, "Error al subir la imagen del grupo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createGroup(groupName: String, groupImageUrl: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val groupId = FirebaseDatabase.getInstance().reference.child("groups").push().key ?: return
        val group = Group(
            id = groupId,
            name = groupName,
            admin = currentUserId,
            members = selectedContacts.map { it.userId }.toMutableList().apply { add(currentUserId) },
            imageUrl = groupImageUrl
        )

        val groupRef = FirebaseDatabase.getInstance().reference.child("groups").child(groupId)
        groupRef.setValue(group).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                finish()
            } else {
                Toast.makeText(this, "Error al crear el grupo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val RC_SELECT_IMAGE = 1
    }
}
