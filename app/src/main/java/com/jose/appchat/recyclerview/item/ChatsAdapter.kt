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

        // Usar la función getContactNameAndEmail para obtener el nombre y el email del contacto
        getContactNameAndEmail(participantId) { userName, userEmail ->
            holder.contactNameTextView.text = userName
            Log.d(TAG, "ParticipantId: $participantId, UserName: $userName, UserEmail: $userEmail")

            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, ChatLogActivity::class.java).apply {
                    putExtra("chatId", chatData.chatId)
                    putExtra("userIdSender", chatData.userId1)
                    putExtra("userIdReceiver", chatData.userId2)
                    putExtra("userName", userName ?: "Desconocido") // Pasar "Desconocido" si userName es null
                    putExtra("userEmail", userEmail ?: "Desconocido") // Pasar "Desconocido" si userEmail es null
                }
                Log.d(TAG, "Nombre del usuario: $userName")
                holder.itemView.context.startActivity(intent)
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

    // Función para obtener el nombre y el email del contacto
    private fun getContactNameAndEmail(userId: String, callback: (String?, String?) -> Unit) {
        // Obtener el userId del usuario actual
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Referencia al nodo de contactos del usuario actual
        val contactsRef = FirebaseDatabase.getInstance().reference.child("contacts").child(currentUserId)

        contactsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var userName: String? = null
                var userEmail: String? = null
                for (contactSnapshot in snapshot.children) {
                    val contactUserId = contactSnapshot.child("userId").getValue(String::class.java)
                    Log.d(TAG, "Id del : $userId")
                    if (contactUserId == userId) {
                        userName = contactSnapshot.child("nombre").getValue(String::class.java)
                        break
                    }
                }
                getEmailFromUserNode(userId) { email ->
                    userEmail = email
                    callback(userName, userEmail)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error de base de datos si es necesario
                Log.e(TAG, "Error al obtener datos de usuario: ${error.message}")
            }
        })
    }

    private fun getEmailFromUserNode(userId: String, callback: (String?) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val email = snapshot.child("email").getValue(String::class.java)
                callback(email)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error de base de datos si es necesario
                Log.e(TAG, "Error al obtener el email del usuario: ${error.message}")
                callback(null)
            }
        })
    }
}
