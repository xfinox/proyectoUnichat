package com.jose.appchat.ui.groups.Informacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jose.appchat.R
import com.jose.appchat.model.User
import de.hdodenhof.circleimageview.CircleImageView

class AdapterInfoGroup(
    private val membersList: List<User>
) : RecyclerView.Adapter<AdapterInfoGroup.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = membersList[position]
        holder.bind(member)
    }

    override fun getItemCount(): Int {
        return membersList.size
    }

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewMemberName)
        private val profileImageView: CircleImageView = itemView.findViewById(R.id.imageViewMemberProfile)

        fun bind(member: User) {
            nameTextView.text = member.name
            Glide.with(itemView.context).load(member.profilePicturePath).placeholder(R.drawable.profile).into(profileImageView)
        }
    }
}