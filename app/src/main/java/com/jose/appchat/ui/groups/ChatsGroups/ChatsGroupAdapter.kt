package com.jose.appchat.ui.groups.ChatsGroups

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jose.appchat.R
import com.jose.appchat.model.MessageGroup
import de.hdodenhof.circleimageview.CircleImageView
import com.jose.appchat.model.Contact_group
import java.text.SimpleDateFormat
import java.util.*

class ChatsGroupAdapter(
    private val messageList: List<MessageGroup>,
    private val currentUserId: String,
    private val contactsMap: Map<String, Contact_group>,
    private val userNamesMap: Map<String, String>,
    private val userPhotosMap: Map<String, String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        if (holder is SentMessageViewHolder) {
            val photoUrl = userPhotosMap[currentUserId] ?: ""
            holder.bind(message, photoUrl)
            Log.d("ChatsGroupAdapter", "SentMessage Photo URL: $photoUrl")
        } else if (holder is ReceivedMessageViewHolder) {
            val contact = contactsMap[message.senderId]
            val senderName = contact?.nombre ?: userNamesMap[message.senderId] ?: "Unknown"
            val photoUrl = userPhotosMap[message.senderId] ?: ""
            holder.bind(message, senderName, photoUrl)
            Log.d("ChatsGroupAdapter", "ReceivedMessage Sender: $senderName, Photo URL: $photoUrl")
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.textViewMessage)
        private val timestampTextView: TextView = itemView.findViewById(R.id.textViewTimestamp)
        private val profileImageView: CircleImageView = itemView.findViewById(R.id.imageViewProfile)

        fun bind(message: MessageGroup, photoUrl: String) {
            messageTextView.text = message.text
            timestampTextView.text = formatTimestamp(message.timestamp)
            Glide.with(itemView.context).load(photoUrl).placeholder(R.drawable.profile).into(profileImageView)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = Date(timestamp)
            return sdf.format(date)
        }
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderTextView: TextView = itemView.findViewById(R.id.textViewSenderName)
        private val messageTextView: TextView = itemView.findViewById(R.id.textViewMessage)
        private val timestampTextView: TextView = itemView.findViewById(R.id.textViewTimestamp)
        private val profileImageView: CircleImageView = itemView.findViewById(R.id.imageViewProfile)

        fun bind(message: MessageGroup, senderName: String, photoUrl: String) {
            senderTextView.text = senderName
            messageTextView.text = message.text
            timestampTextView.text = formatTimestamp(message.timestamp)
            Glide.with(itemView.context).load(photoUrl).placeholder(R.drawable.profile).into(profileImageView)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = Date(timestamp)
            return sdf.format(date)
        }
    }
}