package com.hz_apps.filetimelock.utils

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

fun copyFile(source: File, destination: File) {
    if (!source.exists()) {
        throw FileNotFoundException("Source file does not exist")
    }

    if (destination.exists()) {
        if (!destination.delete()) {
            throw IOException("Failed to delete destination file")
        }
    }

    destination.createNewFile()

    val inputStream = FileInputStream(source)
    val outputStream = FileOutputStream(destination)

    val buffer = ByteArray(1024)
    var bytesRead: Int

    while (true) {
        bytesRead = inputStream.read(buffer)
        if (bytesRead == -1) {
            break
        }

        outputStream.write(buffer, 0, bytesRead)
    }

    inputStream.close()
    outputStream.close()
}

fun deleteFile(file: File): Boolean {
    if (!file.exists()) {
        return true
    }

    return file.delete()
}

fun createFolder(context: Context, folderName: String): Boolean {
    val folderPath = File("/data/data/${context.packageName}/", folderName)
    if (!folderPath.exists()) {
        return folderPath.mkdirs()
    }
    return false
}