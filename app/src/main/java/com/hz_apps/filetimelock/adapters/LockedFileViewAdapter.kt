package com.hz_apps.filetimelock.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.database.LockFile
import com.hz_apps.filetimelock.utils.setFileIcon

class LockedFileViewAdapter (
    private val items : List<LockFile>
) : RecyclerView.Adapter<LockedFileViewAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).
        inflate(R.layout.item_locked_file, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setFileIcon(holder.imageView, items[position].extension)
        holder.name.text = items[position].name
        holder.remainingTime.text = items[position].unlockTime.toString()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_locked_item)
        val name : TextView = itemView.findViewById(R.id.name_locked_item)
        val remainingTime : TextView = itemView.findViewById(R.id.remaining_time_locked_item)
    }
}