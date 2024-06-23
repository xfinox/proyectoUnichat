package com.jose.appchat.recyclerview.item

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.jose.appchat.R
import com.jose.appchat.ui.Chats.ChatLogActivity

class ChatsAdapter(private val chatDataList: List<ListaChatsAdapter.ChatData>) :
    RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    private val TAG = "ChatsAdapter"
    private val contactNameCache = mutableMapOf<String, String?>()
    private val contactImageCache = mutableMapOf<String, String?>()
    private val contactEmailCache = mutableMapOf<String, String?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatData = chatDataList[position]
        holder.bind(chatData)

        // Obtener datos del usuario usando participantId de chatData
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val participantId = if (chatData.userId1 == currentUserId) chatData.userId2 else chatData.userId1
        Log.d(TAG, "ParticipantId: $participantId, ChatId: ${chatData.chatId}")

        // Verificar si el nombre, la imagen y el email del contacto ya están en la caché
        val cachedName = contactNameCache[participantId]
        val cachedImage = contactImageCache[participantId]
        val cachedEmail = contactEmailCache[participantId]
        if (cachedName != null && cachedImage != null && cachedEmail != null) {
            holder.contactNameTextView.text = cachedName
            Glide.with(holder.itemView.context).load(cachedImage).into(holder.contactImageView)
            Log.d(TAG, "Using cached data for ParticipantId: $participantId, UserName: $cachedName, Image: $cachedImage, Email: $cachedEmail")

            holder.itemView.setOnClickListener {
                openChatLogActivity(holder, chatData.chatId, currentUserId, participantId, cachedName, cachedEmail)
            }
        } else {
            // Usar la función getContactDetails para obtener el nombre del contacto
            getContactDetails(participantId) { userName, userEmail ->
                if (userName == null || userEmail == null) {
                    // Si el nombre o el email no se encuentran en "contacts", buscar en "users"
                    getUserDetails(participantId) { name, profileImageUrl, email ->
                        val finalName = name ?: "Desconocido"
                        holder.contactNameTextView.text = finalName
                        Glide.with(holder.itemView.context).load(profileImageUrl).transform(CircleCrop()).into(holder.contactImageView)
                        contactNameCache[participantId] = finalName // Almacenar en la caché
                        contactImageCache[participantId] = profileImageUrl // Almacenar en la caché
                        contactEmailCache[participantId] = email // Almacenar en la caché
                        Log.d("Datos", "ParticipantId: $participantId, UserName: $finalName, Image: $profileImageUrl, Email: $email")

                        // Agregar log para mostrar el ID del chat, la URL de la foto de perfil y el email
                        Log.d("ChatList", "ChatId: ${chatData.chatId}, ProfileImageUrl: $profileImageUrl, Email: $email")

                        holder.itemView.setOnClickListener {
                            openChatLogActivity(holder, chatData.chatId, currentUserId, participantId, finalName, email)
                        }
                    }
                } else {
                    // Obtener la URL de la foto de perfil del nodo "users"
                    getUserProfileImage(participantId) { profileImageUrl ->
                        holder.contactNameTextView.text = userName
                        Glide.with(holder.itemView.context).load(profileImageUrl).transform(
                            CircleCrop()
                        ).into(holder.contactImageView)
                        contactNameCache[participantId] = userName // Almacenar en la caché
                        contactImageCache[participantId] = profileImageUrl // Almacenar en la caché
                        contactEmailCache[participantId] = userEmail // Almacenar en la caché
                        Log.d("Datos", "ParticipantId: $participantId, UserName: $userName, Image: $profileImageUrl, Email: $userEmail")

                        // Agregar log para mostrar el ID del chat y la URL de la foto de perfil
                        Log.d("ChatList", "ChatId: ${chatData.chatId}, ProfileImageUrl: $profileImageUrl, Email: $userEmail")

                        holder.itemView.setOnClickListener {
                            openChatLogActivity(holder, chatData.chatId, currentUserId, participantId, userName, userEmail)
                        }
                    }
                }
            }
        }
    }

    private fun openChatLogActivity(holder: ChatViewHolder, chatId: String?, currentUserId: String?, participantId: String, userName: String?, email: String?) {
        val intent = Intent(holder.itemView.context, ChatLogActivity::class.java).apply {
            putExtra("chatId", chatId)
            putExtra("userIdReceiver", participantId)
            putExtra("userName", userName)
            putExtra("userEmail", email)
        }
        holder.itemView.context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return chatDataList.size
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactNameTextView: TextView = itemView.findViewById(R.id.contactName_chat)
        val contactImageView: ImageView = itemView.findViewById(R.id.contactImage_chat)

        fun bind(chatData: ListaChatsAdapter.ChatData) {
            // No se setea el texto aquí, se hará en onBindViewHolder
        }
    }

    // Función para obtener el nombre y el email del contacto
    private fun getContactDetails(userId: String, callback: (String?, String?) -> Unit) {
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
                    if (contactUserId == userId) {
                        userName = contactSnapshot.child("nombre").getValue(String::class.java)
                        userEmail = contactSnapshot.child("email").getValue(String::class.java)
                        break
                    }
                }
                callback(userName, userEmail)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al obtener datos de usuario: ${error.message}")
            }
        })
    }

    // Función para obtener la URL de la foto de perfil del nodo "users"
    private fun getUserProfileImage(userId: String, callback: (String?) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImagePath = snapshot.child("profilePicturePath").getValue(String::class.java)
                if (profileImagePath != null) {
                    val storageRef = FirebaseStorage.getInstance().reference.child(profileImagePath)
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        Log.d("Datos1", "UserId: $userId, ProfileImageUrl: $uri")
                        callback(uri.toString())
                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to get download URL", exception)
                        callback(null)
                    }
                } else {
                    Log.d(TAG, "UserId: $userId has no profile picture.")
                    callback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al obtener datos de usuario: ${error.message}")
            }
        })
    }

    // Función para obtener los detalles del usuario desde el nodo "users"
    private fun getUserDetails(userId: String, callback: (String?, String?, String?) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("nombre").getValue(String::class.java)
                val profileImagePath = snapshot.child("profilePicturePath").getValue(String::class.java)
                val userEmail = snapshot.child("email").getValue(String::class.java)
                if (profileImagePath != null) {
                    val storageRef = FirebaseStorage.getInstance().reference.child(profileImagePath)
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        Log.d("Datos1", "UserId: $userId, ProfileImageUrl: $uri, Email: $userEmail")
                        callback(userName, uri.toString(), userEmail)
                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to get download URL", exception)
                        callback(userName, null, userEmail)
                    }
                } else {
                    Log.d(TAG, "UserId: $userId has no profile picture.")
                    callback(userName, null, userEmail)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al obtener datos de usuario: ${error.message}")
            }
        })
    }
}
