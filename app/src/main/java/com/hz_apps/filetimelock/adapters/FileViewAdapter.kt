package com.hz_apps.filetimelock.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hz_apps.filetimelock.R
import java.io.File

class FileViewAdapter() : RecyclerView.Adapter<FileViewAdapter.ViewHolder>() {

    private lateinit var files : Array<File>
    private lateinit var file : File
    constructor(file : File) : this() {
        this.file = file
        this.files = file.listFiles()!!
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.file_view, parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.fileName.text = files[position].name

        if (files[position].isDirectory) {
            holder.fileIcon.setImageResource(R.drawable.folder_icon)
        } else {
            Glide.with(holder.itemView.context)
                .load(files[position])
                .into(holder.fileIcon)
        }

        holder.itemView.setOnClickListener {
            if (files[position].isDirectory) {
                this.file = files[position]
                updateFiles()
            }
        }

    }

    fun updateFiles() {
        this.files = file.listFiles()!!
        notifyDataSetChanged()
    }

    fun getCurrentPath() : String? {
        return file.path
    }

    fun setFile(file : File) {
        this.file = file
    }

    fun onBackPressed() {
        this.file = file.parentFile!!
        updateFiles()
    }

    class ViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {
        val fileName : TextView = itemView.findViewById(R.id.name_file_view)
        val fileIcon : ImageView= itemView.findViewById(R.id.icon_file_view)
    }
}