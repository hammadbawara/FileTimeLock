package com.hz_apps.filetimelock.ui.file_picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FilePickerViewModel(
) : ViewModel(), ViewModelProvider.Factory {
    var path = "/storage/emulated/0"
    var isLaunchedAsFileTransfer = false
}