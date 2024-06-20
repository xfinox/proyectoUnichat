package com.jose.appchat.ui.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jose.appchat.R
import com.jose.appchat.model.UserGroups
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class MemberAdapter(
    private val members: MutableList<UserGroups>,
    private val onMemberDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    var showDeleteButton: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.bind(member, showDeleteButton, onMemberDeleteClick)
    }

    override fun getItemCount(): Int {
        return members.size
    }

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewProfile: ImageView = itemView.findViewById(R.id.imageViewProfile)
        private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        private val textViewEmail: TextView = itemView.findViewById(R.id.textViewEmail)
        private val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)

        fun bind(member: UserGroups, showDeleteButton: Boolean, onMemberDeleteClick: (String) -> Unit) {
            textViewName.text = member.name
            textViewEmail.text = member.email
            if (member.profileImageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(member.profileImageUrl)
                    .placeholder(R.drawable.profile)
                    .transform(CropCircleTransformation())
                    .into(imageViewProfile)
            } else {
                imageViewProfile.setImageResource(R.drawable.profile)
            }

            buttonDelete.visibility = if (showDeleteButton) View.VISIBLE else View.GONE
            buttonDelete.setOnClickListener {
                onMemberDeleteClick(member.id)
            }
        }
    }
}
