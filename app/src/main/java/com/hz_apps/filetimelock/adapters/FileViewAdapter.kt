package com.hz_apps.filetimelock.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
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

class FileViewAdapter(
) : RecyclerView.Adapter<FileViewAdapter.ViewHolder>() {

    private lateinit var files: MutableList<File>
    private lateinit var viewModel: FilePickerViewModel
    private lateinit var activity: Activity

    constructor(activity: Activity, viewModel: FilePickerViewModel) : this() {
        this.viewModel = viewModel
        this.files = listFilteredFiles(viewModel.file)
        this.activity = activity;
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.file_linear_view, parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.fileName.text = files[position].name

        val currentFile = files[position]

        if (currentFile.isDirectory) {
            holder.fileIcon.setImageResource(R.drawable.ic_folder)
//            holder.noOfItems.visibility = View.VISIBLE
//            holder.noOfItems.text = "${currentFile.length()} items"
        }else{
            setFileIcon(holder, files[position])
        }



        holder.itemView.setOnClickListener {
            if (files[position].isDirectory) {
                viewModel.file = currentFile
                updateFiles()
            }
            else if (files[position].isFile) {
                val intent = Intent()
                intent.putExtra("result", currentFile)
                activity.setResult(Activity.RESULT_OK, intent)
                activity.finish()
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

    fun getCurrentPath(): String? {
        return viewModel.file.path
    }

    fun onBackPressed() {
        this.viewModel.file = viewModel.file.parentFile!!
        updateFiles()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.name_file_view)
        val fileIcon: ImageView = itemView.findViewById(R.id.icon_file_view)
        val noOfItems : TextView = itemView.findViewById(R.id.no_of_items_file_view)
    }

    fun getFileTypeIcon(file: File, holder: ViewHolder): Int {

        val extension = file.path.substring(file.path.lastIndexOf(".") + 1)

        return when (extension) {
            "jpg", "png", "gif", "jpeg", "JPEG", "PNG", "GIF", "JPG" -> {
                Glide.with(holder.itemView.context)
                    .load(file)
                    .placeholder(R.drawable.ic_image)
                    .into(holder.fileIcon)
                0
            }
            "mp4", "mkv", "avi", "mov", "wmv" -> {
                Glide.with(holder.itemView.context)
                    .load(file)
                    .placeholder(R.drawable.ic_video)
                    .into(holder.fileIcon)
                0
            }
            "mp3", "m4a", "wav", "ogg" -> R.drawable.ic_music
            "zip", "rar", "7z" -> R.drawable.ic_zip
            "pdf" -> R.drawable.ic_pdf
            "apk" -> R.drawable.ic_apk
            else -> R.drawable.ic_unknown_file
        }
    }

    fun setFileIcon(holder: ViewHolder, currentFile: File) {
        val resource = getFileTypeIcon(currentFile, holder)
        if (resource == 0)
            return
        holder.fileIcon.setImageResource(resource)
    }
}