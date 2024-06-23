package com.jose.appchat.ui.status

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.jose.appchat.R
import com.jose.appchat.model.State
import java.text.SimpleDateFormat
import java.util.Locale

class StatusAdapter(
    private val statusList: List<State>,
    private val onItemClick: (String, String) -> Unit
) : RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    private val TAG = "StatusAdapter"

    class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusImageView: ImageView = itemView.findViewById(R.id.imageViewStatus)
        val statusNameTextView: TextView = itemView.findViewById(R.id.textViewStatusName)
        val statusTimeTextView: TextView = itemView.findViewById(R.id.textViewStatusTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_status, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val state = statusList[position]
        Glide.with(holder.itemView.context)
            .load(state.imageUrl)
            .transform(CircleCrop())
            .into(holder.statusImageView)

        holder.statusNameTextView.text = state.username // Mostrar el nombre de usuario

        val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
        val formattedTime = sdf.format(state.timestamp)
        holder.statusTimeTextView.text = formattedTime // Mostrar la hora en que se subió el estado

        holder.itemView.setOnClickListener {
            onItemClick(state.userId, state.username)
        }

        Log.d(TAG, "Bind state: $state")
    }

    override fun getItemCount(): Int {
        return statusList.size
    }
}
