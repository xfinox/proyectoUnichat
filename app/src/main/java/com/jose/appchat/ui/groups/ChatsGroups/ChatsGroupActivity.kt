package com.jose.appchat.ui.groups.ChatsGroups

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.jose.appchat.R
import com.jose.appchat.model.MessageGroup
import com.jose.appchat.model.Contact_group

class ChatsGroupActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: ChatsGroupAdapter
    private val messageList = mutableListOf<MessageGroup>()
    private val contactsMap = mutableMapOf<String, Contact_group>()
    private val userNamesMap = mutableMapOf<String, String>()
    private val userPhotosMap = mutableMapOf<String, String>()
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var groupId: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats_group)

        groupId = intent.getStringExtra("groupId") ?: run {
            Log.e("ChatsGroupActivity", "GroupId is null")
            finish()
            return
        }
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("ChatsGroupActivity", "User is not authenticated")
            finish()
            return
        }

        recyclerView = findViewById(R.id.recyclerViewGroupMessages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = ChatsGroupAdapter(messageList, userId, contactsMap, userNamesMap, userPhotosMap)
        recyclerView.adapter = messageAdapter

        val sendButton: Button = findViewById(R.id.buttonSendGroupMessage)
        val messageEditText: EditText = findViewById(R.id.editTextGroupMessage)

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageEditText.text.clear()
            }
        }

        loadContacts()
        loadGroupMembers()
        loadMessages()
    }

    private fun sendMessage(text: String) {
        val message = MessageGroup(senderId = userId, text = text, timestamp = System.currentTimeMillis())
        Log.d("ChatsGroupActivity", "Sending message: $message")
        databaseReference.child("group_chats").child(groupId).push().setValue(message)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ChatsGroupActivity", "Message sent successfully")
                } else {
                    Log.e("ChatsGroupActivity", "Failed to send message", task.exception)
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            }
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
                    Log.e("ChatsGroupActivity", "Failed to load contacts: ${error.message}")
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
                    Log.e("ChatsGroupActivity", "Failed to load group members: ${error.message}")
                }
            })
    }

    private fun fetchUserDetails(userId: String) {
        databaseReference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("nombre").getValue(String::class.java) ?: "Unknown"
                    val profilePicturePath = snapshot.child("profilePicturePath").getValue(String::class.java)
                    if (profilePicturePath != null && profilePicturePath.isNotEmpty()) {
                        FirebaseStorage.getInstance().reference.child(profilePicturePath).downloadUrl
                            .addOnSuccessListener { uri ->
                                userNamesMap[userId] = name
                                userPhotosMap[userId] = uri.toString()
                                messageAdapter.notifyDataSetChanged()
                                Log.d("ChatsGroupActivity", "User details fetched: Name: $name, Photo URL: ${uri.toString()}")
                            }.addOnFailureListener {
                                Log.e("ChatsGroupActivity", "Failed to get photo URL: ${it.message}")
                            }
                    } else {
                        userNamesMap[userId] = name
                        userPhotosMap[userId] = ""
                        messageAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatsGroupActivity", "Failed to fetch user details: ${error.message}")
                }
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMessages() {
        databaseReference.child("group_chats").child(groupId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(MessageGroup::class.java)
                    Log.d("ChatsGroupActivity", "Message retrieved: $message")
                    message?.let {
                        messageList.add(it)
                        messageAdapter.notifyDataSetChanged()
                        recyclerView.smoothScrollToPosition(messageList.size - 1)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatsGroupActivity", "Failed to load messages: ${error.message}")
                }
            })
    }
}
