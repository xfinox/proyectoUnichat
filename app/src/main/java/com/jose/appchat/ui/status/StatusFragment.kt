package com.jose.appchat.ui.status

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
import com.jose.appchat.model.State

class StatusFragment : Fragment() {

    private lateinit var statusRecyclerView: RecyclerView
    private lateinit var statusAdapter: StatusAdapter
    private lateinit var statusList: MutableList<State>
    private lateinit var fabAddStatus: FloatingActionButton

    private val TAG = "StatusFragment"
    private val ref = FirebaseDatabase.getInstance().getReference("/states")
    private val contactsRef = FirebaseDatabase.getInstance().getReference("/contacts/${FirebaseAuth.getInstance().currentUser?.uid}")

    private val stateListener = object : ValueEventListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onDataChange(snapshot: DataSnapshot) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val contactIds = mutableListOf<String>()
            val contactNames = mutableMapOf<String, String>()
            contactIds.add(userId) // Añadir el propio ID del usuario a la lista

            snapshot.children.forEach {
                val contactId = it.child("userId").value.toString()
                val contactName = it.child("nombre").value.toString()
                contactIds.add(contactId)
                contactNames[contactId] = contactName
            }

            Log.d(TAG, "Fetched contacts: $contactIds")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(stateSnapshot: DataSnapshot) {
                    statusList.clear()
                    var myState: State? = null
                    val currentTime = System.currentTimeMillis()
                    val twentyFourHoursInMillis = 24 * 60 * 60 * 1000

                    stateSnapshot.children.forEach { userSnapshot ->
                        userSnapshot.children.forEach { stateSnapshot ->
                            val state = stateSnapshot.getValue(State::class.java)
                            Log.d(TAG, "Fetched state: $state")
                            if (state != null && currentTime - state.timestamp <= twentyFourHoursInMillis) {
                                if (state.userId == userId) {
                                    state.username = "Mi estado"
                                    myState = state
                                } else if (contactIds.contains(state.userId)) {
                                    state.username = contactNames[state.userId] ?: "Desconocido"
                                    statusList.add(state)
                                    Log.d(TAG, "Added state to list: $state")
                                }
                            }
                        }
                    }

                    myState?.let { statusList.add(0, it) } // Agregar "Mi estado" al principio
                    statusAdapter.notifyDataSetChanged()
                    Log.d(TAG, "Total states added: ${statusList.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to fetch states", error.toException())
                }
            })
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "Failed to fetch contacts", error.toException())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_estados, container, false)

        statusRecyclerView = view.findViewById(R.id.recyclerViewStatus)
        statusRecyclerView.layoutManager = LinearLayoutManager(context)
        statusList = mutableListOf()
        statusAdapter = StatusAdapter(statusList) { userId, username ->
            val intent = Intent(activity, DetailStatusActivity::class.java)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }
        statusRecyclerView.adapter = statusAdapter

        fabAddStatus = view.findViewById(R.id.fabAddStatus)
        fabAddStatus.setOnClickListener {
            val intent = Intent(activity, AddStatusActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        contactsRef.addValueEventListener(stateListener) // Agregar el listener cuando el fragmento se reanuda
    }

    override fun onPause() {
        super.onPause()
        contactsRef.removeEventListener(stateListener) // Quitar el listener cuando el fragmento se pausa
    }
}
