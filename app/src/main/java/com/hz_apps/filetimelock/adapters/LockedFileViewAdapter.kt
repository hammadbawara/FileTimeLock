package com.hz_apps.filetimelock.adapters

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.database.LockFile
import com.hz_apps.filetimelock.utils.setFileIcon
import java.io.File
import java.time.LocalDateTime


class LockedFileViewAdapter (
    private val activity : Activity,
    val lockedFilesList : List<LockFile>,
    private val lockFileListeners: LockFileListeners,
    private var dateNow : LocalDateTime
) : RecyclerView.Adapter<LockedFileViewAdapter.LockedFileViewHolder>(){

    fun updateTimeNow(dateNow: LocalDateTime) {
        this.dateNow = dateNow
    }

    val checkedItems : MutableList<Boolean> = MutableList(lockedFilesList.size){false}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LockedFileViewHolder {
        val view = LayoutInflater.from(parent.context).
        inflate(R.layout.item_locked_file, parent, false)
        return LockedFileViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lockedFilesList.size
    }

    override fun onBindViewHolder(holder: LockedFileViewHolder, position: Int) {
        val file = lockedFilesList[position]
        holder.name.text = file.name

        if (file.isUnlocked) {
            setFileUnlocked(holder, position)
        }else {
            setTimeOnItem(holder, position)
        }

        if (checkedItems[position])
            setItemBackgroundSelected(holder.itemView)
        else
            setItemBackgroundUnselected(holder.itemView)

        holder.itemView.setOnLongClickListener {
            lockFileListeners.onItemLongClicked(holder.itemView, position)
        }
        holder.itemView.setOnClickListener {
            lockFileListeners.onItemClicked(holder.itemView, position)
        }

        holder.moreOptions.setOnClickListener{
            lockFileListeners.onMoreOptionClicked(holder.moreOptions, holder.itemView, position)
        }
    }

    class LockedFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_locked_item)
        val name: TextView = itemView.findViewById(R.id.name_locked_item)
        val remainingTime: TextView = itemView.findViewById(R.id.remaining_time_locked_item)
        val remainingTimeLayout : ConstraintLayout = itemView.findViewById(R.id.remainingTimeLayout)
        val moreOptions : ImageView = itemView.findViewById<ImageView>(R.id.more_options_locked_item)
    }
    private fun setItemBackgroundSelected(item : View) {
        item.setBackgroundColor(activity.resources.getColor(R.color.selected_item_background, activity.theme))
    }
    private fun setItemBackgroundUnselected(item : View) {
        item.setBackgroundColor(Color.TRANSPARENT)
    }
    fun setItemBackground(itemView: View, position : Int){
        if (checkedItems[position])
            setItemBackgroundSelected(itemView)
        else
            setItemBackgroundUnselected(itemView)
    }

    fun setFileUnlocked(holder : LockedFileViewHolder, position: Int) {
        if (holder.remainingTimeLayout.isVisible) {
            holder.remainingTimeLayout.visibility = View.INVISIBLE
        }
        setFileIcon(holder.imageView.context, holder.imageView, File(lockedFilesList[position].path), lockedFilesList[position].extension)
    }

    private fun setTimeOnItem(holder : LockedFileViewHolder, position: Int) {
        val file = lockedFilesList[position]
        file.calculateRemainingTime(dateNow)
        if (file.remainingTime == "unlocked") {
            lockFileListeners.onFileUnlocked(file.id, position)
            setFileUnlocked(holder, position)
        }else{
            if (!holder.remainingTimeLayout.isVisible) {
                holder.remainingTimeLayout.visibility = View.VISIBLE
            }
            holder.remainingTime.text = file.remainingTime
            setFileIcon(holder.imageView, file.extension)
        }

    }
}

interface LockFileListeners {
    fun onItemClicked(itemView : View, position: Int)
    fun onItemLongClicked(itemView : View, position: Int) : Boolean
    fun onFileUnlocked(id: Int, position : Int)
    fun onMoreOptionClicked(moreOptionView: View, itemView : View, position: Int)
}


