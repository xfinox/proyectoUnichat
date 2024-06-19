package com.jose.appchat.recyclerview.item

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListaChatsAdapter {

    // Referencia a la base de datos de Firebase
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    // Tag para los logs
    private val TAG = "ListaChatsAdapter"

    // Obtener el UID del usuario actualmente autenticado
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // Clase de datos que representa la información de un chat
    data class ChatData(val chatId: String, val userId1: String, val userId2: String)

    // Función para verificar los chats en los que participa el usuario actual
    fun verificarChatsUsuario(callback: (List<ChatData>) -> Unit) {
        // Referencia al nodo "Chats" en la base de datos
        val chatsRef = database.child("Chats")

        // Añadir un listener para leer los datos una sola vez
        chatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Lista para almacenar los datos de los chats
                val chatDataList: MutableList<ChatData> = mutableListOf()

                // Iterar sobre cada chat en "Chats"
                for (chatSnapshot in snapshot.children) {
                    val chatId = chatSnapshot.key ?: continue
                    val userIds = chatId.split("-")
                    if (userIds.size == 2) {
                        val userId1 = userIds[0]
                        val userId2 = userIds[1]
                        if (userId1 == currentUserId || userId2 == currentUserId) {
                            chatDataList.add(ChatData(chatId, userId1, userId2))
                        }
                    }
                }
                // Llamar al callback con la lista de datos de los chats
                callback(chatDataList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Loguear el error en caso de fallo
                Log.e(TAG, "Error al verificar los chats del usuario", error.toException())
                // Llamar al callback con una lista vacía
                callback(emptyList())
            }
        })
    }
}
