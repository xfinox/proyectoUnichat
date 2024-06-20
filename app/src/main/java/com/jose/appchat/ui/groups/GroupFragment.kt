package com.jose.appchat.ui.groups

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jose.appchat.R
import com.jose.appchat.model.Group

class GroupFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var btnCreateGroup: Button
    private val groupList = mutableListOf<Group>()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_groups, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewGroups)
        btnCreateGroup = view.findViewById(R.id.btnCreateGroup)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        groupAdapter = GroupAdapter(groupList)
        recyclerView.adapter = groupAdapter

        btnCreateGroup.setOnClickListener {
            startActivity(Intent(context, CreateGroupActivity::class.java))
        }

        loadGroups()

        return view
    }

    private fun loadGroups() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val groupsRef = FirebaseDatabase.getInstance().reference.child("groups")

        groupsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                groupList.clear()
                for (groupSnapshot in snapshot.children) {
                    val group = groupSnapshot.getValue(Group::class.java)
                    if (group != null && group.members.contains(currentUserId)) {
                        groupList.add(group)
                    }
                }
                groupAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error si es necesario
            }
        })
    }
}
