package com.jose.appchat.ui.groups

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jose.appchat.R
import com.jose.appchat.model.Group
import com.jose.appchat.ui.groups.crear.CreateGroupActivity

class GroupFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: ListGroupAdapter
    private val groupList = mutableListOf<Group>()
    private val TAG = "GroupFragment"
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private var userGroupsListener: ChildEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_groups, container, false)

        // Inicializar el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewGroups)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        groupAdapter = ListGroupAdapter(groupList)
        recyclerView.adapter = groupAdapter

        // Configurar el FloatingActionButton para crear grupos
        val fabCreateGroup = view.findViewById<FloatingActionButton>(R.id.fabCreateGroup)
        fabCreateGroup.setOnClickListener {
            val intent = Intent(activity, CreateGroupActivity::class.java)
            startActivity(intent)
        }

        // Cargar y observar los grupos del usuario
        observeUserGroups()

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeUserGroups() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId.isNullOrEmpty()) {
            Toast.makeText(activity, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Usuario no autenticado")
            return
        }

        userGroupsListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val groupId = snapshot.key ?: return
                addGroupById(groupId)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val groupId = snapshot.key ?: return
                updateGroupById(groupId)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val groupId = snapshot.key ?: return
                removeGroupById(groupId)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // No se necesita manejar este caso
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al observar los grupos del usuario: ${error.message}")
            }
        }

        databaseReference.child("user_groups").child(userId).addChildEventListener(userGroupsListener!!)
    }

    private fun addGroupById(groupId: String) {
        databaseReference.child("groups").child(groupId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(groupDetailSnapshot: DataSnapshot) {
                val groupName = groupDetailSnapshot.child("name").getValue(String::class.java)
                val photoUrl = groupDetailSnapshot.child("photoUrl").getValue(String::class.java)
                val members = groupDetailSnapshot.child("members").children.mapNotNull { it.key }
                val rolesSnapshot = groupDetailSnapshot.child("roles")
                val roles = mutableMapOf<String, String>()
                for (role in rolesSnapshot.children) {
                    val key = role.key
                    val value = role.getValue(String::class.java)
                    if (key != null && value != null) {
                        roles[key] = value
                    }
                }

                val group = Group(groupId, groupName, photoUrl, members, roles)
                groupList.add(group)
                Log.d(TAG, "Añadiendo grupo: $groupId")
                groupAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al cargar los detalles del grupo: ${error.message}")
            }
        })
    }

    private fun updateGroupById(groupId: String) {
        val index = groupList.indexOfFirst { it.groupId == groupId }
        if (index != -1) {
            databaseReference.child("groups").child(groupId).addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(groupDetailSnapshot: DataSnapshot) {
                    val groupName = groupDetailSnapshot.child("name").getValue(String::class.java)
                    val photoUrl = groupDetailSnapshot.child("photoUrl").getValue(String::class.java)
                    val members = groupDetailSnapshot.child("members").children.mapNotNull { it.key }
                    val rolesSnapshot = groupDetailSnapshot.child("roles")
                    val roles = mutableMapOf<String, String>()
                    for (role in rolesSnapshot.children) {
                        val key = role.key
                        val value = role.getValue(String::class.java)
                        if (key != null && value != null) {
                            roles[key] = value
                        }
                    }

                    val group = Group(groupId, groupName, photoUrl, members, roles)
                    groupList[index] = group
                    Log.d(TAG, "Actualizando grupo: $groupId")
                    groupAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error al cargar los detalles del grupo: ${error.message}")
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun removeGroupById(groupId: String) {
        val index = groupList.indexOfFirst { it.groupId == groupId }
        if (index != -1) {
            groupList.removeAt(index)
            Log.d(TAG, "Eliminando grupo: $groupId")
            groupAdapter.notifyItemRemoved(index)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userGroupsListener?.let {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                databaseReference.child("user_groups").child(userId).removeEventListener(it)
            }
        }
    }
}
