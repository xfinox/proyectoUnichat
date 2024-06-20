package com.jose.appchat.ui.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale

class GroupPagerAdapter(
    private val groupsList: List<Group>,
    private val onGroupClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupPagerAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groupsList[position]
        holder.bind(group, onGroupClick)
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewGroup: CircleImageView = itemView.findViewById(R.id.imageViewGroup)
        private val textViewGroupName: TextView = itemView.findViewById(R.id.textViewGroupName)
        private val textViewLastMessage: TextView = itemView.findViewById(R.id.textViewLastMessage)

        fun bind(group: Group, onGroupClick: (Group) -> Unit) {
            textViewGroupName.text = group.name
            Picasso.get().load(group.imageUrl).placeholder(R.drawable.profile).into(imageViewGroup)

            // Load last message for the group
            val messagesRef = FirebaseDatabase.getInstance().reference.child("group_chats").child(group.id).limitToLast(1)
            messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(MessageGroup::class.java)
                        message?.let {
                            val lastMessageText = "${it.senderName}: ${it.text} - ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it.timestamp)}"
                            textViewLastMessage.text = lastMessageText
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if necessary
                }
            })

            itemView.setOnClickListener {
                onGroupClick(group)
            }
        }
    }
}
