package com.hz_apps.filetimelock.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.adapters.ImageAndNameAdapter
import com.hz_apps.filetimelock.adapters.OnImageAndTextClickListener
import com.hz_apps.filetimelock.databinding.FragmentHomeBinding
import com.hz_apps.filetimelock.models.ItemWithImageAndName
import com.hz_apps.filetimelock.ui.files.FilesActivity

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private var clickListener : OnImageAndTextClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    @SuppressLint("Recycle")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val items = ArrayList<ItemWithImageAndName>()
        val images = resources.obtainTypedArray(R.array.home_menu_items_icons)
        val names : Array<String> = resources.getStringArray(R.array.home_menu_items_names)

        for (i in 0..1) {
            val item = ItemWithImageAndName()
            item.imageResId = images.getResourceId(i, -1)
            item.name = names[i]
            items.add(item)
        }

        setItemClickListener()

        val adapter = ImageAndNameAdapter(items, clickListener)

        binding.homeRecyclerView.adapter = adapter

        images.recycle()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setItemClickListener() {
        clickListener = object : OnImageAndTextClickListener {
            override fun onItemClick(position: Int) {
                when (position) {
                    0 -> {
                        val intent = Intent(activity, FilesActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}