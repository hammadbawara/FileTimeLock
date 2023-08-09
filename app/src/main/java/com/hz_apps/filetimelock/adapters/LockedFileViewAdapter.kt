package com.hz_apps.filetimelock.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.database.LockFile
import com.hz_apps.filetimelock.utils.calculateTimeDifference
import com.hz_apps.filetimelock.utils.setFileIcon
import java.time.LocalDateTime


class LockedFileViewAdapter (
    private val activity : Activity,
    val items : List<LockFile>,
    private val clickListenerLockedFile: ClickListenerLockedFile
) : RecyclerView.Adapter<LockedFileViewAdapter.ViewHolder>(){

    private var actionMode : ActionMode.Callback? = null
    val checkedItems : MutableList<Boolean> = MutableList(items.size){false}

    private var timeNow : LocalDateTime = LocalDateTime.now()
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
        holder.remainingTime.text = calculateTimeDifference(timeNow, items[position].unlockTime)

        if (checkedItems[position])
            setItemBackgroundSelected(holder.itemView)
        else
            setItemBackgroundUnselected(holder.itemView)

        holder.itemView.setOnLongClickListener {
            clickListenerLockedFile.onItemLongClicked(holder.itemView, position)
        }
        holder.itemView.setOnClickListener {
            clickListenerLockedFile.onItemClicked(holder.itemView, position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_locked_item)
        val name: TextView = itemView.findViewById(R.id.name_locked_item)
        val remainingTime: TextView = itemView.findViewById(R.id.remaining_time_locked_item)
    }

    fun setItemSelected(item : View, position: Int) {
        if (actionMode == null) {
            return
        }
        if (checkedItems[position]) {
            setItemBackgroundUnselected(item)
            checkedItems[position] = false
        }else {
            checkedItems[position] = true
            setItemBackgroundSelected(item)
        }
        notifyItemChanged(position)
    }
    private fun setItemBackgroundSelected(item : View) {

        item.setBackgroundColor(activity.getColor(R.color.light_blue))
    }
    private fun setItemBackgroundUnselected(item : View) {
        item.setBackgroundColor(activity.getColor(R.color.white))
    }
}

interface ClickListenerLockedFile {
    fun onItemClicked(itemView : View, position: Int)
    fun onItemLongClicked(itemView : View, position: Int) : Boolean
}


