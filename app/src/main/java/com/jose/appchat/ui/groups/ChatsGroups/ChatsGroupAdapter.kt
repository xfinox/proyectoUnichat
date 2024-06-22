package com.jose.appchat.ui.groups.ChatsGroups

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jose.appchat.R
import com.jose.appchat.model.MessageGroup
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import java.text.SimpleDateFormat
import java.util.*

class ChatsGroupAdapter(
    private val messages: List<MessageGroup>,
    private val currentUserId: String,
    private val contactsMap: Map<String, String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SENDER = 1
        const val VIEW_TYPE_RECEIVER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENDER else VIEW_TYPE_RECEIVER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENDER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sender_message, parent, false)
            SenderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receiver_message, parent, false)
            ReceiverViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == VIEW_TYPE_SENDER) {
            (holder as SenderViewHolder).bind(messages[position])
        } else {
            (holder as ReceiverViewHolder).bind(messages[position])
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderProfileImageView: ImageView = itemView.findViewById(R.id.senderProfileImageView)
        private val senderNameTextView: TextView = itemView.findViewById(R.id.senderNameTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(message: MessageGroup) {
            val displayName = contactsMap[message.senderId] ?: message.senderName
            senderNameTextView.text = displayName
            messageTextView.text = message.text
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = sdf.format(Date(message.timestamp))
            timestampTextView.text = formattedTime

            if (message.senderProfileUrl.isNotEmpty()) {
                Picasso.get().load(message.senderProfileUrl).placeholder(R.drawable.profile).transform(CropCircleTransformation()).into(senderProfileImageView)
            } else {
                senderProfileImageView.setImageResource(R.drawable.profile)
            }
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderProfileImageView: ImageView = itemView.findViewById(R.id.senderProfileImageView)
        private val senderNameTextView: TextView = itemView.findViewById(R.id.senderNameTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(message: MessageGroup) {
            val displayName = contactsMap[message.senderId] ?: message.senderName
            senderNameTextView.text = displayName
            messageTextView.text = message.text
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = sdf.format(Date(message.timestamp))
            timestampTextView.text = formattedTime

            if (message.senderProfileUrl.isNotEmpty()) {
                Picasso.get().load(message.senderProfileUrl).placeholder(R.drawable.profile).transform(CropCircleTransformation()).into(senderProfileImageView)
            } else {
                senderProfileImageView.setImageResource(R.drawable.profile)
            }
        }
    }
}
