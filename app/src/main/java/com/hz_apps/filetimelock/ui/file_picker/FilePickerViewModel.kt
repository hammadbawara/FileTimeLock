package com.hz_apps.filetimelock.ui.file_picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.io.File

class FilePickerViewModel (
) : ViewModel(), ViewModelProvider.Factory {
    val path = "/storage/emulated/0"
    var file = File(path)
}