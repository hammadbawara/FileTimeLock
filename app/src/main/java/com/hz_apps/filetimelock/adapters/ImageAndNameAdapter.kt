package com.hz_apps.filetimelock.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.models.ItemWithImageAndName

class ImageAndNameAdapter(
    private val items: List<ItemWithImageAndName>,
    private val clickListener : OnImageAndTextClickListener? = null
) : RecyclerView.Adapter<ImageAndNameAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_image_and_name_view,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.imageView.setImageResource(item.imageResId)
        holder.textView.text = item.name

        holder.itemView.setOnClickListener(View.OnClickListener {
            clickListener?.onItemClick(position)
        })
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image_view)
        val textView: TextView = itemView.findViewById(R.id.item_text_view);
    }
}

interface OnImageAndTextClickListener {
    fun onItemClick(position: Int)
}