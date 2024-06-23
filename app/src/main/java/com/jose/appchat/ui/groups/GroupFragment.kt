package com.jose.appchat.ui.groups

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jose.appchat.R
import com.jose.appchat.model.Group
import com.jose.appchat.ui.groups.ChatsGroups.ChatsGroupActivity
import com.jose.appchat.ui.groups.crear.CreateGroupActivity
import com.jose.appchat.ui.groups.lista.ListGroupAdapter

class GroupFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: ListGroupAdapter
    private lateinit var groupList: MutableList<Group>
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var fab: FloatingActionButton
    private lateinit var groupsListener: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_groups, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        groupList = mutableListOf()
        groupAdapter = ListGroupAdapter(groupList) { group ->
            openGroupChat(group)
        }
        recyclerView.adapter = groupAdapter

        fab = view.findViewById(R.id.fab_add_group)
        fab.setOnClickListener {
            val intent = Intent(activity, CreateGroupActivity::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        loadGroups()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        database.getReference("groups").removeEventListener(groupsListener)
    }

    private fun loadGroups() {
        val userId = auth.currentUser?.uid ?: return

        groupsListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                groupList.clear()
                for (dataSnapshot in snapshot.children) {
                    val group = dataSnapshot.getValue(Group::class.java)
                    group?.let {
                        it.id = dataSnapshot.key ?: ""

                        // Extraer los miembros correctamente
                        val members = mutableListOf<String>()
                        dataSnapshot.child("members").children.forEach { memberSnapshot ->
                            memberSnapshot.getValue(String::class.java)?.let { member ->
                                members.add(member)
                            }
                        }

                        if (members.contains(userId)) {
                            it.members = members
                            groupList.add(it)
                        }
                    }
                }
                groupAdapter.notifyDataSetChanged()
                Log.d("GroupFragment", "Loaded Groups: $groupList")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GroupFragment", "Error loading groups", error.toException())
            }
        }

        database.getReference("groups").addValueEventListener(groupsListener)
    }

    private fun openGroupChat(group: Group) {
        val intent = Intent(activity, ChatsGroupActivity::class.java).apply {
            putExtra("GROUP_ID", group.id)
        }
        startActivity(intent)
    }
}
