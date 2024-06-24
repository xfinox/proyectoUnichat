package com.jose.appchat.ui.groups.crear

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.jose.appchat.R
import com.jose.appchat.model.Contact_group

class AdapterCreateGroup(
    private val contacts: List<Contact_group>,
    private val onContactSelected: (Contact_group) -> Unit
) : RecyclerView.Adapter<AdapterCreateGroup.ContactViewHolder>() {

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.contactName)
        val emailTextView: TextView = itemView.findViewById(R.id.contactEmail)
        val profileImageView: ImageView = itemView.findViewById(R.id.contactImage)
        val checkBox: CheckBox = itemView.findViewById(R.id.contactCheckBox)

        fun bind(contact: Contact_group) {
            nameTextView.text = contact.nombre
            emailTextView.text = contact.email

            if (contact.profilePicturePath.isNotEmpty()) {
                val baseUrl = "https://firebasestorage.googleapis.com/v0/b/unichatfb.appspot.com/o"
                val profilePicturePath = contact.profilePicturePath.replace("/", "%2F")
                val profilePictureUrl = "$baseUrl$profilePicturePath?alt=media"
                Glide.with(profileImageView.context)
                    .load(profilePictureUrl)
                    .transform(CircleCrop())
                    .placeholder(R.drawable.profile)
                    .into(profileImageView)
                Log.d("ContactSelection1", "Selected: ${contact.nombre}, Foto URL: $profilePictureUrl")
            } else {
                profileImageView.setImageResource(R.drawable.profile)
                Log.d("ContactSelection", "Selected: ${contact.nombre}, Foto: No photo available")
            }

            checkBox.isChecked = contact.isSelected

            itemView.setOnClickListener {
                contact.isSelected = !contact.isSelected
                checkBox.isChecked = contact.isSelected
                onContactSelected(contact)
                Log.d("ContactSelection", "Selected: ${contact.nombre}, UserID: ${contact.userId}, isSelected: ${contact.isSelected}")
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                contact.isSelected = isChecked
                onContactSelected(contact)
                Log.d("ContactSelection", "Selected: ${contact.nombre}, UserID: ${contact.userId}, isSelected: ${contact.isSelected}")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact_group, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int = contacts.size
}
