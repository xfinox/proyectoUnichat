package com.jose.appchat.ui.status

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jose.appchat.R
import com.jose.appchat.model.State

class DetailAdapterStatus(private val statusList: List<State>) : RecyclerView.Adapter<DetailAdapterStatus.StatusViewHolder>() {

    class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusImageView: ImageView = itemView.findViewById(R.id.imageViewStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_status_detail, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val state = statusList[position]
        Glide.with(holder.itemView.context)
            .load(state.imageUrl)
            .into(holder.statusImageView)
    }

    override fun getItemCount(): Int {
        return statusList.size
    }
}
