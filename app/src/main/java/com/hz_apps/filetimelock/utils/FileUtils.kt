package com.hz_apps.filetimelock.utils

import android.content.Context
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

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

fun createFolder(context: Context, folderName: String): Boolean {
    val folderPath = File("/data/data/${context.packageName}/", folderName)
    if (!folderPath.exists()) {
        return folderPath.mkdirs()
    }
    return false
}

fun runShellCommand(command: String): String {
    val process = ProcessBuilder()
        .command("sh", "-c", command)
        .redirectErrorStream(true)
        .start()

    val output = StringBuilder()
    val reader = BufferedReader(InputStreamReader(process.inputStream))

    var line: String?
    while (reader.readLine().also { line = it } != null) {
        output.append(line).append("\n")
    }

    reader.close()
    process.waitFor()

    return output.toString()
}