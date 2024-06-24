package com.jose.appchat.ui.groups

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.jose.appchat.R
import com.jose.appchat.model.Group
import com.jose.appchat.ui.groups.ChatsGroups.ChatsGroupActivity

class ListGroupAdapter(private val groupList: List<Group>) :
    RecyclerView.Adapter<ListGroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupImageView: ImageView = itemView.findViewById(R.id.groupImageView)
        val groupNameTextView: TextView = itemView.findViewById(R.id.groupNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groupList[position]
        holder.groupNameTextView.text = group.name
        Glide.with(holder.itemView.context)
            .load(group.photoUrl)
            .transform(CircleCrop())
            .into(holder.groupImageView)

        // Implementar el clic para mostrar el ID del grupo
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ChatsGroupActivity::class.java)
            intent.putExtra("groupId", group.groupId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return groupList.size
    }
}
