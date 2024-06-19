package com.jose.appchat.recyclerview.item

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jose.appchat.R
import com.jose.appchat.ui.Chats.ChatLogActivity

class ChatsAdapter(private val chatDataList: List<ListaChatsAdapter.ChatData>) :
    RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    private val TAG = "ChatsAdapter"
    private val contactNameCache = mutableMapOf<String, String?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatData = chatDataList[position]
        holder.bind(chatData)

        // Obtener datos del usuario usando participantId de chatData
        val participantId = if (chatData.userId1 == FirebaseAuth.getInstance().currentUser?.uid) chatData.userId2 else chatData.userId1
        Log.d(TAG, "ParticipantId: $participantId")

        // Verificar si el nombre del contacto ya está en la caché
        val cachedName = contactNameCache[participantId]
        if (cachedName != null) {
            holder.contactNameTextView.text = cachedName
            Log.d(TAG, "Using cached name for ParticipantId: $participantId, UserName: $cachedName")

            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, ChatLogActivity::class.java).apply {
                    putExtra("chatId", chatData.chatId)
                    putExtra("userIdSender", chatData.userId1)
                    putExtra("userIdReceiver", chatData.userId2)
                    putExtra("userName", cachedName ?: "Desconocido") // Pasar "Desconocido" si userName es null
                }
                holder.itemView.context.startActivity(intent)
            }
        } else {
            // Usar la función getContactDetails para obtener el nombre y el email del contacto
            getContactDetails(participantId) { userName ->
                holder.contactNameTextView.text = userName
                contactNameCache[participantId] = userName // Almacenar en la caché
                Log.d(TAG, "ParticipantId: $participantId, UserName: $userName")

                holder.itemView.setOnClickListener {
                    val intent = Intent(holder.itemView.context, ChatLogActivity::class.java).apply {
                        putExtra("chatId", chatData.chatId)
                        putExtra("userIdSender", chatData.userId1)
                        putExtra("userIdReceiver", chatData.userId2)
                        putExtra("userName", userName ?: "Desconocido") // Pasar "Desconocido" si userName es null
                    }
                    holder.itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return chatDataList.size
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactNameTextView: TextView = itemView.findViewById(R.id.contactName_chat)

        fun bind(chatData: ListaChatsAdapter.ChatData) {
            // No se setea el texto aquí, se hará en onBindViewHolder
        }
    }

    // Función para obtener el nombre del contacto
    private fun getContactDetails(userId: String, callback: (String?) -> Unit) {
        // Obtener el userId del usuario actual
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Referencia al nodo de contactos del usuario actual
        val contactsRef = FirebaseDatabase.getInstance().reference.child("contacts").child(currentUserId)

        contactsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var userName: String? = null
                for (contactSnapshot in snapshot.children) {
                    val contactUserId = contactSnapshot.child("userId").getValue(String::class.java)
                    if (contactUserId == userId) {
                        userName = contactSnapshot.child("nombre").getValue(String::class.java)
                        break
                    }
                }
                if (userName == null) {
                    getUserDetails(userId) { name ->
                        callback(name)
                    }
                } else {
                    callback(userName)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al obtener datos de usuario: ${error.message}")
            }
        })
    }

    private fun getUserDetails(userId: String, callback: (String?) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("nombre").getValue(String::class.java)
                callback(userName)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al obtener datos de usuario: ${error.message}")
            }
        })
    }
}
