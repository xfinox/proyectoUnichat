package com.jose.appchat.ui.groups

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.jose.appchat.R
import com.jose.appchat.model.MessageGroup
import com.squareup.picasso.Picasso

class GroupChatActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<MessageGroup>()
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageButton
    private lateinit var imageViewGroup: ImageView
    private lateinit var toolbarTitle: TextView

    private lateinit var groupId: String
    private lateinit var groupName: String
    private var currentUserProfileUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerViewMessages)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        imageViewGroup = findViewById(R.id.imageViewGroup)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        groupId = intent.getStringExtra("groupId") ?: ""
        groupName = intent.getStringExtra("groupName") ?: ""

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Aquí se quita el título

        toolbarTitle.text = groupName

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter

        // Load group image
        loadGroupImage()

        // Pre-almacenar la URL de la imagen de perfil del usuario actual
        fetchUserProfileUrl()

        buttonSend.setOnClickListener {
            sendMessage()
        }

        loadMessages()

        // Navegar al DetailGroupActivity al hacer clic en la imagen del grupo
        imageViewGroup.setOnClickListener {
            val intent = Intent(this, DetailGroupActivity::class.java).apply {
                putExtra("groupId", groupId)
            }
            startActivity(intent)
        }
    }

    private fun loadGroupImage() {
        val groupRef = FirebaseDatabase.getInstance().reference.child("groups").child(groupId)
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageUrl = snapshot.child("imageUrl").value.toString()
                if (imageUrl.isNotEmpty()) {
                    Picasso.get().load(imageUrl).placeholder(R.drawable.profile).into(imageViewGroup)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if necessary
            }
        })
    }

    private fun fetchUserProfileUrl() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usersRef = FirebaseDatabase.getInstance().reference.child("users").child(currentUserId)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profilePicturePath = snapshot.child("profilePicturePath").value.toString()
                if (profilePicturePath.isNotEmpty()) {
                    val storageRef = FirebaseStorage.getInstance().reference.child(profilePicturePath)
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        currentUserProfileUrl = uri.toString()
                    }.addOnFailureListener {
                        // Handle error if necessary
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if necessary
            }
        })
    }

    private fun loadMessages() {
        val messagesRef = FirebaseDatabase.getInstance().reference.child("group_chats").child(groupId)
        messagesRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(MessageGroup::class.java)
                    message?.let { messageList.add(it) }
                }
                messageAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messageList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if necessary
            }
        })
    }

    private fun sendMessage() {
        val messageText = editTextMessage.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Unknown"

            val messagesRef = FirebaseDatabase.getInstance().reference.child("group_chats").child(groupId)
            val messageId = messagesRef.push().key ?: return
            val message = MessageGroup(
                id = messageId,
                senderId = currentUserId,
                senderName = currentUserName,
                senderProfileUrl = currentUserProfileUrl,
                text = messageText,
                timestamp = System.currentTimeMillis()
            )
            messagesRef.child(messageId).setValue(message)
            editTextMessage.text.clear()
        }
    }
}
