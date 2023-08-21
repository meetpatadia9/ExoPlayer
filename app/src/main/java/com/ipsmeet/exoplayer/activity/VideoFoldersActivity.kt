package com.ipsmeet.exoplayer.activity

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ipsmeet.exoplayer.adapter.VideoFolderAdapter
import com.ipsmeet.exoplayer.databinding.ActivityVideoFoldersBinding
import com.ipsmeet.exoplayer.dataclass.MediaFileDataClass
import com.ipsmeet.exoplayer.viewmodel.PermissionViewModel
import com.ipsmeet.exoplayer.viewmodel.VideoFolderListViewModel

class VideoFoldersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoFoldersBinding

    private lateinit var permissionViewModel: PermissionViewModel
    private lateinit var viewModel: VideoFolderListViewModel
    private var fileList = arrayListOf<MediaFileDataClass>()
    private var folderPath = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoFoldersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.title = "Video Player"

        permissionViewModel = ViewModelProvider(this)[PermissionViewModel::class.java]
        viewModel = ViewModelProvider(this)[VideoFolderListViewModel::class.java]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        binding.swipeRefresh.setOnRefreshListener {
            displayFolders()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private val storagePermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                displayFolders()
            } else {
                permissionViewModel.requestPermission(this)
            }
        }

    private fun displayFolders() {
        fileList = viewModel.fetchMedia(this, folderPath)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@VideoFoldersActivity, LinearLayoutManager.VERTICAL, false)
            adapter = VideoFolderAdapter(this@VideoFoldersActivity, folderPath)
        }
    }
}