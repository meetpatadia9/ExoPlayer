package com.ipsmeet.exoplayer.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ipsmeet.exoplayer.adapter.VideoFileAdapter
import com.ipsmeet.exoplayer.databinding.ActivityVideoListBinding
import com.ipsmeet.exoplayer.dataclass.MediaFileDataClass
import com.ipsmeet.exoplayer.viewmodel.VideoListViewModel

class VideoListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoListBinding

    private lateinit var viewModel: VideoListViewModel
    private lateinit var folderName: String
    private var videoList = arrayListOf<MediaFileDataClass>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[VideoListViewModel::class.java]

        folderName = intent.getStringExtra("folderName").toString()
        supportActionBar!!.title = folderName
        showVideoFiles()

        binding.swipeRefresh.setOnRefreshListener {
            showVideoFiles()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun showVideoFiles() {
        videoList = viewModel.fetchMedia(this@VideoListActivity, folderName)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@VideoListActivity, LinearLayoutManager.VERTICAL, false)
            adapter = VideoFileAdapter(this@VideoListActivity, videoList)
        }
    }

}