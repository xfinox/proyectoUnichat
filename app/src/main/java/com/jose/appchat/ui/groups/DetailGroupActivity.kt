package com.jose.appchat.ui.groups

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jose.appchat.R
import com.jose.appchat.model.UserGroups
import com.squareup.picasso.Picasso

class DetailGroupActivity : AppCompatActivity() {

    private lateinit var imageViewGroup: ImageView
    private lateinit var textViewGroupName: TextView
    private lateinit var recyclerViewMembers: RecyclerView
    private lateinit var memberAdapter: MemberAdapter

    private lateinit var groupId: String
    private var groupAdmin: String = ""
    private val memberList = mutableListOf<UserGroups>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_group)

        imageViewGroup = findViewById(R.id.imageViewGroup)
        textViewGroupName = findViewById(R.id.textViewGroupName)
        recyclerViewMembers = findViewById(R.id.recyclerViewMembers)

        groupId = intent.getStringExtra("groupId") ?: ""

        recyclerViewMembers.layoutManager = LinearLayoutManager(this)
        memberAdapter = MemberAdapter(memberList) { memberId ->
            // Lógica para eliminar miembro si eres el admin
            if (memberId != groupAdmin) {
                removeMemberFromGroup(memberId)
            }
        }
        recyclerViewMembers.adapter = memberAdapter

        loadGroupDetails()
        loadGroupMembers()
    }

    private fun loadGroupDetails() {
        val groupRef = FirebaseDatabase.getInstance().reference.child("groups").child(groupId)
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groupName = snapshot.child("name").value.toString()
                val imageUrl = snapshot.child("imageUrl").value.toString()
                groupAdmin = snapshot.child("admin").value.toString()

                textViewGroupName.text = groupName
                if (imageUrl.isNotEmpty()) {
                    Picasso.get().load(imageUrl).placeholder(R.drawable.profile).into(imageViewGroup)
                }

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId == groupAdmin) {
                    memberAdapter.showDeleteButton = true
                } else {
                    memberAdapter.showDeleteButton = false
                }
                memberAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if necessary
            }
        })
    }

    private fun loadGroupMembers() {
        val membersRef = FirebaseDatabase.getInstance().reference.child("groups").child(groupId).child("members")
        membersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                memberList.clear()
                for (memberSnapshot in snapshot.children) {
                    val memberId = memberSnapshot.value.toString()
                    loadUserDetails(memberId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if necessary
            }
        })
    }

    private fun loadUserDetails(userId: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserGroups::class.java)
                user?.let {
                    it.id = userId // Ensure the id is set correctly
                    memberList.add(it)
                    if (it.id == groupAdmin) {
                        memberList.remove(it)
                        memberList.add(0, it) // Move admin to the top
                    }
                }
                memberAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if necessary
            }
        })
    }

    private fun removeMemberFromGroup(memberId: String) {
        val groupRef = FirebaseDatabase.getInstance().reference.child("groups").child(groupId).child("members").orderByValue().equalTo(memberId)
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (memberSnapshot in snapshot.children) {
                    memberSnapshot.ref.removeValue().addOnSuccessListener {
                        // Remove member from the list and update the adapter
                        memberList.removeAll { it.id == memberId }
                        memberAdapter.notifyDataSetChanged()
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
}
