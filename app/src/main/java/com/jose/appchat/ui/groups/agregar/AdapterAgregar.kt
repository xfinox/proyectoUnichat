package com.jose.appchat.ui.groups.agregar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jose.appchat.R
import com.jose.appchat.model.Contact_group

class AdapterAgregar(
    private val contactList: List<Contact_group>,
    private val onAddMemberClick: (Contact_group) -> Unit
) : RecyclerView.Adapter<AdapterAgregar.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add, parent, false)
        return ContactViewHolder(view, onAddMemberClick)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.bind(contact)
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    class ContactViewHolder(itemView: View, private val onAddMemberClick: (Contact_group) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.textViewContactName)
        private val emailTextView: TextView = itemView.findViewById(R.id.textViewContactEmail)
        private val addButton: Button = itemView.findViewById(R.id.buttonAddToGroup)

        fun bind(contact: Contact_group) {
            nameTextView.text = contact.nombre
            emailTextView.text = contact.email
            addButton.setOnClickListener {
                onAddMemberClick(contact)
            }
        }
    }
}
