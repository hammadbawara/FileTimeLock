package com.hz_apps.filetimelock.ui.file_picker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.ui.lock_file.LockFileActivity
import java.io.File

/**
 * A fragment representing a list of Items.
 */
class FileViewFragment : Fragment(), MyFileViewRecyclerViewAdapter.OnFileViewInteractionListener{

    private var columnCount = 1
    private val viewModel by lazy { ViewModelProvider(requireActivity())[FilePickerViewModel::class.java] }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_files_list, container, false)
        var path = arguments?.getString("path")

        if (path == null) {
            path = "/storage/emulated/0"
        }
        viewModel.path = path

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = MyFileViewRecyclerViewAdapter(getFiles(path), this@FileViewFragment)
            }
        }
        return view
    }

    // get folder and files from path. Folder show first, then files. And they are in alphabetic order
    private fun getFiles(path: String): List<File> {
        val allFiles = File(path).listFiles()
        val folders = mutableListOf<File>()
        val files = mutableListOf<File>()
        for (file in allFiles!!) {
            if (file.isDirectory) {
                folders.add(file)
            } else {
                files.add(file)
            }
        }
        folders.sortBy { it.name }
        files.sortBy { it.name }
        return folders + files
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            FileViewFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

    override fun onClick(file : File){
        if (file.isDirectory) {
            val fragment = FileViewFragment.newInstance(1)
            val bundle = Bundle()
            bundle.putString("path", file.absolutePath)
            fragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit()
        }else{
            if(!viewModel.isLaunchedAsFileTransfer) {
                val intent = Intent(activity, LockFileActivity::class.java)
                intent.putExtra("file_path", file.absolutePath)
                startActivity(intent)
                activity?.finish()
            }
        }
    }
}