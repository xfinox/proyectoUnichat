package com.jose.appchat.ui.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jose.appchat.R
import com.jose.appchat.model.Contact_group
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class ContactsAdapter(
    private val contactsList: List<Pair<Contact_group, String?>>,
    private val onContactSelected: (Contact_group, Boolean) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact_group, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val (contact, profilePicturePath) = contactsList[position]
        holder.bind(contact, profilePicturePath, onContactSelected)
    }

    override fun getItemCount(): Int {
        return contactsList.size
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewProfile: ImageView = itemView.findViewById(R.id.imageViewProfile)
        private val textViewContactName: TextView = itemView.findViewById(R.id.textViewContactName)
        private val checkBoxSelect: CheckBox = itemView.findViewById(R.id.checkBoxSelect)

        fun bind(contact: Contact_group, profilePicturePath: String?, onContactSelected: (Contact_group, Boolean) -> Unit) {
            textViewContactName.text = contact.nombre
            if (!profilePicturePath.isNullOrEmpty()) {
                Picasso.get()
                    .load(profilePicturePath)
                    .placeholder(R.drawable.profile)
                    .transform(CropCircleTransformation())
                    .into(imageViewProfile)
            } else {
                imageViewProfile.setImageResource(R.drawable.profile)
            }

            checkBoxSelect.setOnCheckedChangeListener { _, isChecked ->
                onContactSelected(contact, isChecked)
            }
        }
    }
}
