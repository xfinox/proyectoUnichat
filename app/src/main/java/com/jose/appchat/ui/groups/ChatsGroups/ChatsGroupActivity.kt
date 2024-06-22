package com.jose.appchat.ui.groups.ChatsGroups

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.jose.appchat.R
import com.jose.appchat.model.Group
import com.jose.appchat.model.MessageGroup
import com.jose.appchat.model.Contact
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class ChatsGroupActivity : AppCompatActivity() {

    private lateinit var groupImageView: ImageView
    private lateinit var groupNameTextView: TextView
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatsGroupAdapter: ChatsGroupAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var groupId: String
    private val messagesList: MutableList<MessageGroup> = mutableListOf()
    private val contactsMap: MutableMap<String, String> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats_group)

        groupImageView = findViewById(R.id.groupImageView)
        groupNameTextView = findViewById(R.id.groupNameTextView)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatsGroupAdapter = ChatsGroupAdapter(messagesList, FirebaseAuth.getInstance().currentUser?.uid ?: "", contactsMap)
        chatRecyclerView.adapter = chatsGroupAdapter

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        groupId = intent.getStringExtra("GROUP_ID")!!
        Log.e("ChatsGroupActivity", "ID group $groupId")

        loadContacts()
        checkIfUserIsMember(groupId)

        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun loadContacts() {
        val userId = auth.currentUser?.uid ?: return
        val contactsRef = database.child("contacts").child(userId)
        contactsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (contactSnapshot in snapshot.children) {
                    val contact = contactSnapshot.getValue(Contact::class.java)
                    contact?.let {
                        contactsMap[it.userId] = it.nombre
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatsGroupActivity", "Error loading contacts", error.toException())
            }
        })
    }

    private fun checkIfUserIsMember(groupId: String) {
        val userId = auth.currentUser?.uid ?: return

        val groupRef = database.child("groups").child(groupId)
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val group = snapshot.getValue(Group::class.java)
                if (group != null && group.members.contains(userId)) {
                    Log.e("ChatsGroupActivity", "Group Data: $group")
                    groupNameTextView.text = group.name
                    Picasso.get().load(group.photoUrl).placeholder(R.drawable.profile)
                        .transform(CropCircleTransformation()).into(groupImageView)

                    loadMessages(groupId)
                } else {
                    Toast.makeText(this@ChatsGroupActivity, "You are not a member of this group", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatsGroupActivity", "Error loading group info", error.toException())
            }
        })
    }

    private fun loadMessages(groupId: String) {
        val messagesRef = database.child("group_messages").child(groupId)
        messagesRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                messagesList.clear()
                for (postSnapshot in snapshot.children) {
                    val message = postSnapshot.getValue(MessageGroup::class.java)
                    message?.let { messagesList.add(it) }
                }
                chatsGroupAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatsGroupActivity", "Error loading messages", error.toException())
            }
        })
    }

    private fun sendMessage() {
        val text = messageEditText.text.toString()
        if (text.isBlank()) return

        val currentUser = auth.currentUser ?: return
        val message = MessageGroup(
            id = "",
            text = text,
            senderId = currentUser.uid,
            senderName = currentUser.displayName ?: "Unknown",
            senderProfileUrl = currentUser.photoUrl?.toString() ?: "",
            timestamp = System.currentTimeMillis()
        )

        val messagesRef = database.child("group_messages").child(groupId).push()
        message.id = messagesRef.key ?: ""
        messagesRef.setValue(message).addOnSuccessListener {
            messageEditText.text.clear()
        }.addOnFailureListener { e ->
            Log.e("ChatsGroupActivity", "Error sending message", e)
        }
    }



}
