package com.jose.appchat.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jose.appchat.model.UserChats

object FirestoreUtil {
    private val firebaseDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val currentUserRef: DatabaseReference
        get() = firebaseDatabase.reference.child("users").child(FirebaseAuth.getInstance().currentUser?.uid
            ?: throw NullPointerException("UID is null."))

    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val newUserChats = UserChats(
            name = currentUser?.displayName ?: "",
            bio = "",
            profilePicturePath = null,
            uid = currentUser?.uid ?: "",
            email = currentUser?.email ?: ""
        )

        currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    currentUserRef.setValue(newUserChats.toMap()).addOnSuccessListener {
                        onComplete()
                    }.addOnFailureListener {
                        // Manejar el fallo si es necesario
                    }
                } else {
                    onComplete()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar la cancelación si es necesario
                onComplete()
            }
        })
    }

    fun updateCurrentUser(name: String = "", bio: String = "", profilePicturePath: String? = null, onComplete: () -> Unit) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (bio.isNotBlank()) userFieldMap["bio"] = bio
        if (profilePicturePath != null) userFieldMap["profilePicturePath"] = profilePicturePath

        currentUserRef.updateChildren(userFieldMap).addOnSuccessListener {
            onComplete()
        }.addOnFailureListener {
            // Manejar el fallo si es necesario
            onComplete()
        }
    }

    fun getCurrentUser(onComplete: (UserChats?) -> Unit) {
        currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userChats = snapshot.getValue(UserChats::class.java)
                onComplete(userChats)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error si es necesario
                onComplete(null)
            }
        })
    }
}
