package com.jose.appchat.ui.groups.lista

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jose.appchat.R
import com.jose.appchat.model.Group
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class ListGroupAdapter(
    private val groups: List<Group>,
    private val onGroupClick: (Group) -> Unit
) : RecyclerView.Adapter<ListGroupAdapter.GroupViewHolder>() {

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupNameTextView: TextView = itemView.findViewById(R.id.groupName)
        val groupImageView: ImageView = itemView.findViewById(R.id.groupImage)

        fun bind(group: Group) {
            groupNameTextView.text = group.name
            if (group.photoUrl.isNotEmpty()) {
                Picasso.get().load(group.photoUrl).placeholder(R.drawable.profile).transform(
                    CropCircleTransformation()
                ).into(groupImageView)
            } else {
                groupImageView.setImageResource(R.drawable.profile)
            }

            itemView.setOnClickListener {
                onGroupClick(group)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    override fun getItemCount(): Int = groups.size
}
