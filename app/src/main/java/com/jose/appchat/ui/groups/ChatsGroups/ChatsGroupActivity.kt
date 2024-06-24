package com.jose.appchat.ui.groups.ChatsGroups

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.jose.appchat.R
import com.jose.appchat.model.MessageGroup
import com.jose.appchat.model.Contact_group
import de.hdodenhof.circleimageview.CircleImageView
import com.jose.appchat.ui.groups.Informacion.InfoGroupActivity
import com.jose.appchat.ui.groups.agregar.AgregarActivity

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

        val toolbar: Toolbar = findViewById(R.id.toolbarGroupChat)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val groupNameTextView: TextView = findViewById(R.id.textViewGroupName)
        val groupPhotoImageView: CircleImageView = findViewById(R.id.imageViewGroupPhoto)

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

        loadGroupDetails(groupNameTextView, groupPhotoImageView)
        loadContacts()
        loadGroupMembers()
        loadMessages()

        setupKeyboardListener()
    }

    private fun setupKeyboardListener() {
        val rootView = findViewById<RecyclerView>(R.id.recyclerViewGroupMessages)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = rootView.rootView.height - rootView.height
            if (heightDiff > 0.25 * rootView.rootView.height) {
                rootView.scrollToPosition(messageList.size - 1)
            }
        }
    }

    private fun loadGroupDetails(groupNameTextView: TextView, groupPhotoImageView: CircleImageView) {
        databaseReference.child("groups").child(groupId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groupName = snapshot.child("name").getValue(String::class.java) ?: "Group Name"
                    val profilePictureUrl = snapshot.child("photoUrl").getValue(String::class.java)
                    groupNameTextView.text = groupName
                    if (!profilePictureUrl.isNullOrEmpty()) {
                        Glide.with(this@ChatsGroupActivity).load(profilePictureUrl).into(groupPhotoImageView)
                        Log.d("ChatsGroupActivity", "Group Photo URL: $profilePictureUrl")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatsGroupActivity", "Failed to load group details: ${error.message}")
                }
            })
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
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Fetching the user's name from the database
                    val name = snapshot.child("nombre").getValue(String::class.java) ?: "Unknown"
                    // Fetching the profile picture path from the database
                    val profilePicturePath = snapshot.child("profilePicturePath").getValue(String::class.java)
                    if (profilePicturePath != null && profilePicturePath.isNotEmpty()) {
                        // Converting the profile picture path to a full URL using FirebaseStorage
                        FirebaseStorage.getInstance().reference.child(profilePicturePath).downloadUrl
                            .addOnSuccessListener { uri ->
                                // Storing the user's name and photo URL in the maps
                                userNamesMap[userId] = name
                                userPhotosMap[userId] = uri.toString()
                                messageAdapter.notifyDataSetChanged()
                                Log.d("ChatsGroupActivity", "User details fetched: Name: $name, Photo URL: ${uri.toString()}")
                            }.addOnFailureListener {
                                Log.e("ChatsGroupActivity", "Failed to get photo URL: ${it.message}")
                            }
                    } else {
                        // If no profile picture is found, storing the user's name and an empty photo URL
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_group_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_info -> {
                // Acción para "Información"
                val intent = Intent(this, InfoGroupActivity::class.java)
                intent.putExtra("groupId", groupId)
                startActivity(intent)
                true
            }
            R.id.action_add -> {
                // Acción para "Agregar"
                val intent = Intent(this, AgregarActivity::class.java)
                intent.putExtra("groupId", groupId)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
