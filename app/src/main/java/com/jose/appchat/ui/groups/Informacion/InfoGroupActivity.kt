package com.jose.appchat.ui.groups.Informacion

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.jose.appchat.R
import com.jose.appchat.model.Contact_group
import com.jose.appchat.model.User
import de.hdodenhof.circleimageview.CircleImageView

class InfoGroupActivity : AppCompatActivity() {

    private lateinit var groupId: String
    private lateinit var groupNameTextView: TextView
    private lateinit var groupPhotoImageView: CircleImageView
    private lateinit var membersRecyclerView: RecyclerView
    private lateinit var membersAdapter: AdapterInfoGroup
    private val membersList = mutableListOf<User>()
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val contactsMap = mutableMapOf<String, Contact_group>()
    private val userNamesMap = mutableMapOf<String, String>()
    private val userPhotosMap = mutableMapOf<String, String>()
    private lateinit var userId: String
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_group)

        groupId = intent.getStringExtra("groupId") ?: run {
            Log.e("InfoGroupActivity", "GroupId is null")
            finish()
            return
        }
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("InfoGroupActivity", "User is not authenticated")
            finish()
            return
        }

        groupNameTextView = findViewById(R.id.textViewGroupName)
        groupPhotoImageView = findViewById(R.id.imageViewGroupPhoto)
        membersRecyclerView = findViewById(R.id.recyclerViewGroupMembers)
        membersRecyclerView.layoutManager = LinearLayoutManager(this)
        membersAdapter = AdapterInfoGroup(membersList)
        membersRecyclerView.adapter = membersAdapter

        val buttonEditGroupName: Button = findViewById(R.id.buttonEditGroupName)
        val buttonEditGroupPhoto: Button = findViewById(R.id.buttonEditGroupPhoto)

        buttonEditGroupName.setOnClickListener {
            editGroupName()
        }

        buttonEditGroupPhoto.setOnClickListener {
            pickImageFromGallery()
        }

        loadGroupDetails()
        loadContacts()
        loadGroupMembers()
    }

    private fun loadGroupDetails() {
        databaseReference.child("groups").child(groupId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groupName = snapshot.child("name").getValue(String::class.java) ?: "Group Name"
                    val profilePictureUrl = snapshot.child("photoUrl").getValue(String::class.java)
                    groupNameTextView.text = groupName
                    if (!profilePictureUrl.isNullOrEmpty()) {
                        Glide.with(this@InfoGroupActivity).load(profilePictureUrl).into(groupPhotoImageView)
                        Log.d("InfoGroupActivity", "Group Photo URL: $profilePictureUrl")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InfoGroupActivity", "Failed to load group details: ${error.message}")
                }
            })
    }

    private fun loadContacts() {
        databaseReference.child("contacts").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        val contact = it.getValue(Contact_group::class.java)
                        contact?.let { contactsMap[contact.userId] = contact }
                    }
                    loadGroupMembers()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InfoGroupActivity", "Failed to load contacts: ${error.message}")
                }
            })
    }

    private fun loadGroupMembers() {
        databaseReference.child("groups").child(groupId).child("members")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        val memberId = it.key ?: return@forEach
                        fetchUserDetails(memberId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InfoGroupActivity", "Failed to load group members: ${error.message}")
                }
            })
    }

    private fun fetchUserDetails(userId: String) {
        databaseReference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java) ?: return
                    val contactName = contactsMap[user.uid]?.nombre
                    val displayName = when {
                        user.uid == this@InfoGroupActivity.userId -> "Yo"
                        contactName != null -> contactName
                        else -> user.name
                    }
                    if (!user.profilePicturePath.isNullOrEmpty()) {
                        FirebaseStorage.getInstance().reference.child(user.profilePicturePath!!).downloadUrl
                            .addOnSuccessListener { uri ->
                                userNamesMap[user.uid] = displayName
                                userPhotosMap[user.uid] = uri.toString()
                                addMemberToList(User(user.uid, displayName, user.bio, uri.toString()))
                                Log.d("InfoGroupActivity", "User details fetched: Name: $displayName, Photo URL: ${uri.toString()}")
                            }.addOnFailureListener {
                                Log.e("InfoGroupActivity", "Failed to get photo URL: ${it.message}")
                            }
                    } else {
                        userNamesMap[user.uid] = displayName
                        userPhotosMap[user.uid] = ""
                        addMemberToList(User(user.uid, displayName, user.bio, ""))
                        Log.d("InfoGroupActivity", "User details fetched: Name: $displayName, Photo URL: none")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InfoGroupActivity", "Failed to fetch user details: ${error.message}")
                }
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addMemberToList(user: User) {
        if (membersList.none { it.uid == user.uid }) {
            membersList.add(user)
            membersAdapter.notifyDataSetChanged()
            Log.d("InfoGroupActivity", "Members List: $membersList")
        }
    }

    private fun editGroupName() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Group Name")

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_group_name, null)
        builder.setView(view)

        val groupNameEditText = view.findViewById<EditText>(R.id.editTextGroupName)

        builder.setPositiveButton("Save") { dialog, _ ->
            val newGroupName = groupNameEditText.text.toString().trim()
            if (newGroupName.isNotEmpty()) {
                databaseReference.child("groups").child(groupId).child("name").setValue(newGroupName)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            groupNameTextView.text = newGroupName
                            Toast.makeText(this, "Group name updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to update group name", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val uri: Uri = data.data!!
            uploadImageToFirebase(uri)
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference.child("group_photos/$groupId")
        storageReference.putFile(uri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    updateGroupPhotoUrl(downloadUri.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateGroupPhotoUrl(url: String) {
        databaseReference.child("groups").child(groupId).child("photoUrl").setValue(url)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Glide.with(this).load(url).into(groupPhotoImageView)
                    Toast.makeText(this, "Group photo updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update group photo", Toast.LENGTH_SHORT).show()
                }
            }
    }
}