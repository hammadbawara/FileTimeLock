package com.hz_apps.filetimelock.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.hz_apps.filetimelock.R
import java.io.File

fun getFileExtension(file: File) : String {
    return file.path.substring(file.path.lastIndexOf(".") + 1)
}

fun getFileType(extension : String) : String {
    val fileType = when (extension.lowercase()) {
        in arrayOf("jpg", "png", "gif", "jpeg") -> "image"
        in arrayOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "mpg", "mpeg", "3gp", "rm", "rmvb", "asf", "swf") -> "video/*"
        in arrayOf("mp3", "m4a", "wav", "ogg", "aac", "flac", "wma", "aiff", "amr", "opus") -> "audio/*"
        in arrayOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "z", "tgz", "tar.gz", "tar.bz2") -> ""
        "pdf" -> "pdf"
        else -> "unknown"
    }
    return fileType
}

fun getFileIcon(extension : String) : Int {
    val resource = when (extension.lowercase()) {
        in arrayOf("jpg", "png", "gif", "jpeg") -> R.drawable.ic_image
        in arrayOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "mpg", "mpeg", "3gp", "rm", "rmvb", "asf", "swf") -> R.drawable.ic_video
        in arrayOf("mp3", "m4a", "wav", "ogg", "aac", "flac", "wma", "aiff", "amr", "opus") -> R.drawable.ic_music
        in arrayOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "z", "tgz", "tar.gz", "tar.bz2") -> R.drawable.ic_zip
        "pdf" -> R.drawable.ic_pdf
        "apk" -> R.drawable.ic_apk
        else -> R.drawable.ic_unknown_file
    }
    return resource
}
fun setFileIcon(context: Context, imageView: ImageView, file: File) {
    val extension = getFileExtension(file)

    val resource = getFileIcon(extension)

    if (resource == R.drawable.ic_image || resource == R.drawable.ic_video) {
        Glide.with(context)
            .load(file)
            .placeholder(resource)
            .into(imageView)
    } else {
        imageView.setImageResource(resource)
    }
}

fun setFileIcon(imageView : ImageView, extension : String) {
    val resource = getFileIcon(extension)
    imageView.setImageResource(resource)
}

