import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.jose.appchat.R
import com.jose.appchat.model.Status
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatusAdapter(private val statusList: List<Status>) :
    RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    inner class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewStatus: ImageView = itemView.findViewById(R.id.imageViewStatus)
        val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_status, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val status = statusList[position]
        Picasso.get().load(status.imageUrl).into(holder.imageViewStatus)

        // Mostrar el botón de eliminar solo para el propietario del estado
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        holder.buttonDelete.visibility = if (status.userId == currentUserId) View.VISIBLE else View.GONE

        // Formatear y mostrar la fecha y hora
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = Date(status.timestamp)
        holder.textViewTimestamp.text = sdf.format(date)

        holder.buttonDelete.setOnClickListener {
            deleteStatus(status.id, status.userId)
        }
    }

    override fun getItemCount(): Int {
        return statusList.size
    }

    private fun deleteStatus(statusId: String, userId: String) {
        val statusesRef = FirebaseDatabase.getInstance().reference.child("statuses").child(userId).child(statusId)
        statusesRef.removeValue().addOnSuccessListener {
            // Estado eliminado correctamente
        }.addOnFailureListener {
            // Manejar el error si es necesario
        }
    }
}
