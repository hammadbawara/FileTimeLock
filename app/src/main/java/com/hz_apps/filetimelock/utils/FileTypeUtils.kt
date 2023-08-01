package com.hz_apps.filetimelock.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.hz_apps.filetimelock.R
import java.io.File
fun setFileIcon(context : Context, imageView : ImageView, file: File) {

    val extension = file.path.substring(file.path.lastIndexOf(".") + 1)

    val resource = when (extension) {
        "jpg", "png", "gif", "jpeg", "JPEG", "PNG", "GIF", "JPG" -> {
            Glide.with(context)
                .load(file)
                .placeholder(R.drawable.ic_image)
                .into(imageView)
            0
        }
        "mp4", "mkv", "avi", "mov", "wmv" -> {
            Glide.with(context)
                .load(file)
                .placeholder(R.drawable.ic_video)
                .into(imageView)
            0
        }
        "mp3", "m4a", "wav", "ogg" -> R.drawable.ic_music
        "zip", "rar", "7z" -> R.drawable.ic_zip
        "pdf" -> R.drawable.ic_pdf
        "apk" -> R.drawable.ic_apk
        else -> R.drawable.ic_unknown_file
    }

    if (resource == 0)
        return

    imageView.setImageResource(resource)


}