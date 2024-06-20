package com.jose.appchat.recyclerview.item

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.jose.appchat.R
import com.jose.appchat.model.MessageChats
import java.text.SimpleDateFormat
import java.util.Date

class ChatMessageAdapter(
    private val chatId: String,
    private val currentUserId: String,
    private val userIdReceiver: String
) : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    private val messagesList: MutableList<MessageChats> = mutableListOf()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val messagesRef: DatabaseReference = database.child("Chats").child(chatId).child("messages")

    init {
        loadMessages()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messagesList[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)

        fun bind(messageChats: MessageChats) {
            messageText.text = messageChats.text

            // Formatear el tiempo
            val sdf = SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            val date = Date(messageChats.time)
            val formattedTime = sdf.format(date)

            messageTime.text = formattedTime

            // Ajustar estilo según el remitente del mensaje
            if (messageChats.senderId == currentUserId) {
                messageText.setBackgroundResource(R.drawable.message_background_sender)
                messageContainer.gravity = Gravity.END
            } else {
                messageText.setBackgroundResource(R.drawable.message_background_receiver)
                messageContainer.gravity = Gravity.START
            }
        }
    }

    private fun loadMessages() {
        messagesRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                messagesList.clear()
                for (postSnapshot in snapshot.children) {
                    val messageChats = postSnapshot.getValue(MessageChats::class.java)
                    messageChats?.let {
                        messagesList.add(it)
                    }
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al cargar los mensajes", error.toException())
            }
        })
    }

    fun sendMessage(messageText: String) {
        val currentTime = System.currentTimeMillis()
        val messageChats = MessageChats(
            messageId = "", // Se generará después
            text = messageText,
            senderId = currentUserId,
            receiverId = userIdReceiver, // Añadir el receiverId
            time = currentTime
        )

        // Generar nueva referencia con ID único para el mensaje
        val newMessageRef = messagesRef.push()
        val messageId = newMessageRef.key ?: return

        // Actualizar el ID del mensaje en el objeto Message
        messageChats.messageId = messageId

        // Establecer el valor del mensaje en la referencia generada
        newMessageRef.setValue(messageChats)
            .addOnSuccessListener {
                // Aquí puedes realizar acciones adicionales después de enviar el mensaje, si es necesario
                Log.d(TAG, "Mensaje enviado correctamente con messageId: $messageId")
            }
            .addOnFailureListener { e ->
                // Manejar el error de envío del mensaje aquí
                Log.e(TAG, "Error al enviar el mensaje", e)
            }
    }
}
