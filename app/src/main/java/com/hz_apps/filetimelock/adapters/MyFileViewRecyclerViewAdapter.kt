package com.hz_apps.filetimelock.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.databinding.FragmentFileBinding
import com.hz_apps.filetimelock.ui.file_picker.placeholder.PlaceholderContent.PlaceholderItem
import com.hz_apps.filetimelock.utils.getFileExtension
import com.hz_apps.filetimelock.utils.setFileIcon
import java.io.File

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyFileViewRecyclerViewAdapter(
    private val filesList: List<File>,
    private val listener: OnFileViewInteractionListener
) : RecyclerView.Adapter<MyFileViewRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentFileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = filesList[position]
        holder.name.text = file.name
        if (file.isDirectory) {
            holder.icon.setImageResource(R.drawable.ic_folder)
        }else{
            setFileIcon(holder.icon.context, holder.icon, file, getFileExtension(file))
        }
        holder.itemView.setOnClickListener{
            listener.onClick(file)
        }
    }

    override fun getItemCount(): Int = filesList.size

    inner class ViewHolder(binding: FragmentFileBinding) : RecyclerView.ViewHolder(binding.root) {
        val icon = binding.iconFileView
        val name = binding.nameFileView
//
//        override fun toString(): String {
//            return super.toString() + " '" + contentView.text + "'"
//        }
    }

    interface OnFileViewInteractionListener {
        fun onClick(file: File)
    }

}