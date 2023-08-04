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
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.ui.file_picker.FilePickerViewModel
import com.hz_apps.filetimelock.utils.setFileIcon
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
            R.layout.item_file_view_linear, parent, false
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
            setFileIcon(holder.itemView.context, holder.fileIcon, files[position])
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
}