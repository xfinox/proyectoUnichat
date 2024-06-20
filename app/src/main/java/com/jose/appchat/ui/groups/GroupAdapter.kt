package com.jose.appchat.ui.groups

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jose.appchat.R
import com.jose.appchat.model.Group
import com.jose.appchat.model.MessageGroup
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class GroupAdapter(private val groupList: List<Group>) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groupList[position]
        holder.bind(group)
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    class GroupViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val imageViewGroup: ImageView = itemView.findViewById(R.id.imageViewGroup)
        private val textViewGroupName: TextView = itemView.findViewById(R.id.textViewGroupName)
        private val textViewLastMessage: TextView = itemView.findViewById(R.id.textViewLastMessage)

        fun bind(group: Group) {
            textViewGroupName.text = group.name
            if (group.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(group.imageUrl)
                    .placeholder(R.drawable.profile)
                    .transform(CropCircleTransformation())
                    .into(imageViewGroup)
            } else {
                imageViewGroup.setImageResource(R.drawable.profile)
            }

            // Load the last message
            loadLastMessage(group)

            itemView.setOnClickListener {
                val intent = Intent(context, GroupChatActivity::class.java)
                intent.putExtra("groupId", group.id)
                intent.putExtra("groupName", group.name)
                context.startActivity(intent)
            }
        }

        private fun loadLastMessage(group: Group) {
            val messagesRef = FirebaseDatabase.getInstance().reference.child("group_chats").child(group.id)
                .orderByKey().limitToLast(1)

            messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(MessageGroup::class.java)
                        message?.let {
                            val displayMessage = "${it.senderName}: ${it.text} - ${android.text.format.DateFormat.format("hh:mm a", it.timestamp)}"
                            textViewLastMessage.text = displayMessage
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if necessary
                }
            })
        }
    }
}
