package com.hz_apps.filetimelock.utils

import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.hz_apps.filetimelock.database.LockFile
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URLConnection


fun createFolder(context: Context, folderName: String): Boolean {
    val folderPath = File("data/data/${context.packageName}/data", folderName)
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

fun openLockFile(context: Context, lockFile: LockFile) {
    val file = File(lockFile.path)
    val contentUri = FileProvider.getUriForFile(context, "com.hz_apps.filetimelock.FileProvider", file)
    val intent = Intent(Intent.ACTION_VIEW)
    val fileType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(lockFile.extension)
    intent.setDataAndType(contentUri, fileType)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(intent)
}

fun shareFile(context: Context, file: File) {
    val intent = Intent(Intent.ACTION_SEND)
    val contentUri = FileProvider.getUriForFile(context, "com.hz_apps.filetimelock.FileProvider", file)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)


// Determine MIME type using HttpURLConnection
    val mimeType = URLConnection.guessContentTypeFromName(file.name)
    intent.setDataAndType(contentUri, mimeType)

    context.startActivity(Intent.createChooser(intent, "Share file"))

}