import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jose.appchat.R
import com.jose.appchat.model.Contact
class ContactAdapter(
    private val contactList: List<Contact>,
    private val clickListener: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.bind(contact, clickListener)
    }

    override fun getItemCount(): Int = contactList.size

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.contactName)
        private val emailTextView: TextView = itemView.findViewById(R.id.contactEmail)

        fun bind(contact: Contact, clickListener: (Contact) -> Unit) {
            nameTextView.text = contact.nombre
            emailTextView.text = contact.email
            itemView.setOnClickListener { clickListener(contact) }
        }
    }
}
