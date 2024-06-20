package com.jose.appchat.ui.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.jose.appchat.R
import com.jose.appchat.model.MessageGroup
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(private val messageList: List<MessageGroup>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
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
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage)
        private val textViewSenderName: TextView = itemView.findViewById(R.id.textViewSenderName)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
        private val imageViewProfile: CircleImageView = itemView.findViewById(R.id.imageViewProfile)

        fun bind(message: MessageGroup) {
            textViewMessage.text = message.text
            textViewSenderName.text = message.senderName
            textViewTimestamp.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(message.timestamp)
            if (message.senderProfileUrl.isNotEmpty()) {
                Picasso.get().load(message.senderProfileUrl).placeholder(R.drawable.profile).into(imageViewProfile)
            }
        }
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage)
        private val textViewSenderName: TextView = itemView.findViewById(R.id.textViewSenderName)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
        private val imageViewProfile: CircleImageView = itemView.findViewById(R.id.imageViewProfile)

        fun bind(message: MessageGroup) {
            textViewMessage.text = message.text
            textViewSenderName.text = message.senderName
            textViewTimestamp.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(message.timestamp)
            if (message.senderProfileUrl.isNotEmpty()) {
                Picasso.get().load(message.senderProfileUrl).placeholder(R.drawable.profile).into(imageViewProfile)
            }
        }
    }
}
