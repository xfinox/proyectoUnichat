package com.jose.appchat.ui.groups.agregar

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.jose.appchat.R
import com.jose.appchat.model.Contact_group

class AgregarActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterAgregar
    private val contactList = mutableListOf<Contact_group>()
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var groupId: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar)

        groupId = intent.getStringExtra("groupId") ?: run {
            finish()
            return
        }
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            finish()
            return
        }

        recyclerView = findViewById(R.id.recyclerViewContacts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdapterAgregar(contactList) { contact ->
            addMemberToGroup(contact)
        }
        recyclerView.adapter = adapter

        loadContacts()
    }

    private fun loadContacts() {
        databaseReference.child("contacts").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    contactList.clear()
                    snapshot.children.forEach {
                        val contact = it.getValue(Contact_group::class.java)
                        contact?.let {
                            contactList.add(contact)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun addMemberToGroup(contact: Contact_group) {
        // Example: Adding a member to the group
        val groupMembersRef = databaseReference.child("groups").child(groupId).child("members")
        val userGroupsRef = databaseReference.child("user_groups").child(contact.userId)

        // Check if the user is already a member of the group
        groupMembersRef.child(contact.userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User is already a member, show message
                    showToast("El usuario ya es miembro del grupo")
                } else {
                    // Add member to the group members list
                    groupMembersRef.child(contact.userId).setValue(true)
                        .addOnSuccessListener {
                            // Add group membership for the user
                            userGroupsRef.child(groupId).setValue(true)
                                .addOnSuccessListener {
                                    showToast("Usuario agregado al grupo")
                                    // Handle success
                                }
                                .addOnFailureListener { error ->
                                    showToast("Error al agregar usuario al grupo: ${error.message}")
                                    // Handle failure
                                }
                        }
                        .addOnFailureListener { error ->
                            showToast("Error al agregar usuario al grupo: ${error.message}")
                            // Handle failure
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error al verificar usuario: ${error.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
