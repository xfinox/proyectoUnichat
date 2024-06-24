package com.jose.appchat.ui.groups.crear

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.jose.appchat.R
import com.jose.appchat.model.Contact_group
import java.util.*

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var contactsAdapter: AdapterCreateGroup
    private lateinit var contactList: MutableList<Contact_group>
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var groupPhotoImageView: ImageView
    private lateinit var groupNameEditText: EditText
    private var selectedImageUri: Uri? = null
    private var contactsListener: ValueEventListener? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            groupPhotoImageView.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        groupPhotoImageView = findViewById(R.id.groupPhoto)
        groupNameEditText = findViewById(R.id.groupName)
        groupPhotoImageView.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        recyclerView = findViewById(R.id.recyclerViewContacts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        contactList = mutableListOf()
        contactsAdapter = AdapterCreateGroup(contactList) { contact ->
            val selectedContact = contactList.find { it.id == contact.id }
            selectedContact?.isSelected = contact.isSelected
        }
        recyclerView.adapter = contactsAdapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("contacts").child(auth.currentUser!!.uid)

        loadContacts()

        val createButton: Button = findViewById(R.id.buttonCreateGroup)
        createButton.setOnClickListener {
            createGroup()
        }
    }

    private fun loadContacts() {
        contactsListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                contactList.clear()
                for (dataSnapshot in snapshot.children) {
                    val contact = dataSnapshot.getValue(Contact_group::class.java)
                    contact?.let {
                        contactList.add(it)
                    }
                }
                contactsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                if (auth.currentUser != null) {
                    Toast.makeText(this@CreateGroupActivity, "Failed to load contacts", Toast.LENGTH_SHORT).show()
                }
            }
        }
        database.addValueEventListener(contactsListener!!)
    }

    private fun createGroup() {
        val groupName = groupNameEditText.text.toString().trim()
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese el nombre del grupo", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Por favor, seleccione una foto para el grupo", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedContacts = contactList.filter { it.isSelected }.map { it.userId }
        if (selectedContacts.isEmpty()) {
            Toast.makeText(this, "Por favor, seleccione al menos un contacto", Toast.LENGTH_SHORT).show()
            return
        }

        val creatorId = auth.currentUser!!.uid
        val groupId = UUID.randomUUID().toString()
        val groupRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId)
        val userGroupsRef = FirebaseDatabase.getInstance().getReference("user_groups")

        val members = selectedContacts.toMutableList()
        members.add(creatorId)

        val roles = hashMapOf<String, String>()
        roles[creatorId] = "admin"

        val groupData = hashMapOf(
            "name" to groupName,
            "members" to members.associateWith { true },
            "roles" to roles
        )

        val storageRef = FirebaseStorage.getInstance().reference.child("group_photos").child(groupId)
        storageRef.putFile(selectedImageUri!!).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                groupData["photoUrl"] = uri.toString()
                groupRef.setValue(groupData).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateUserGroups(creatorId, groupId)
                        members.forEach { updateUserGroups(it, groupId) }
                        Toast.makeText(this, "Grupo creado exitosamente", Toast.LENGTH_SHORT).show()
                        sendGroupCreatedBroadcast()
                        finish()
                    } else {
                        Toast.makeText(this, "Error al crear el grupo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al subir la foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserGroups(userId: String, groupId: String) {
        val userGroupsRef = FirebaseDatabase.getInstance().getReference("user_groups").child(userId)
        userGroupsRef.child(groupId).setValue(true)
    }

    private fun sendGroupCreatedBroadcast() {
        val intent = Intent("com.jose.appchat.GROUP_CREATED")
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        contactsListener?.let { database.removeEventListener(it) }
    }
}
