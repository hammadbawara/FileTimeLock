package com.hz_apps.filetimelock.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.ui.file_picker.FilePickerViewModel
import java.io.File

class FileViewAdapter() : RecyclerView.Adapter<FileViewAdapter.ViewHolder>() {

    private lateinit var files : MutableList<File>
    private lateinit var viewModel : FilePickerViewModel
    constructor(viewModel : FilePickerViewModel) : this() {
        this.viewModel = viewModel
        this.files = listFilteredFiles(viewModel.file)
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
                viewModel.file = files[position]
                updateFiles()
            }
        }

    }

    private fun listFilteredFiles(file: File): MutableList<File> {
        val filteredFiles = mutableListOf<File>()
        for (currentFile in file.listFiles()!!) {
            if (!currentFile.name.startsWith(".")) filteredFiles.add(currentFile)
        }
        return filteredFiles
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFiles() {
        this.files = listFilteredFiles(viewModel.file)
        notifyDataSetChanged()
    }

    fun getCurrentPath() : String? {
        return viewModel.file.path
    }

    fun onBackPressed() {
        this.viewModel.file = viewModel.file.parentFile!!
        updateFiles()
    }

    class ViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {
        val fileName : TextView = itemView.findViewById(R.id.name_file_view)
        val fileIcon : ImageView= itemView.findViewById(R.id.icon_file_view)
    }
}