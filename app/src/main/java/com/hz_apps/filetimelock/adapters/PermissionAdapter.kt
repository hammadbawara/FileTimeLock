package com.hz_apps.filetimelock.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.models.CustomPermission

class PermissionAdapter (
    private val activity : Activity,
    private val permissions: ArrayList<CustomPermission>
): RecyclerView.Adapter<PermissionAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.permission_request_view,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return permissions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.permissionName.text = permissions[position].name
        holder.permissionDescription.text = permissions[position].description
        holder.permissionIcon.setImageResource(permissions[position].icon)
        holder.allowBtn.setOnClickListener{
            val array = IntArray(0)
            activity.onRequestPermissionsResult(1, arrayOf( permissions[position].permission), array)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val permissionName: TextView = itemView.findViewById(R.id.permission_name_textView)
        val permissionDescription: TextView = itemView.findViewById(R.id.permission_desc_textView)
        val permissionIcon: ImageView = itemView.findViewById(R.id.permission_icon_image)
        val allowBtn : Button = itemView.findViewById(R.id.allow_btn_permission)
    }



}