package com.ipsmeet.exoplayer.activity

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ipsmeet.exoplayer.R
import com.ipsmeet.exoplayer.adapter.MusicListAdapter
import com.ipsmeet.exoplayer.databinding.ActivityMusicListBinding
import com.ipsmeet.exoplayer.dataclass.MusicDataClass
import com.ipsmeet.exoplayer.viewmodel.MusicListViewModel
import com.ipsmeet.exoplayer.viewmodel.PermissionViewModel

@UnstableApi
class MusicListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMusicListBinding
    private lateinit var permissionViewModel: PermissionViewModel
    private lateinit var viewModel: MusicListViewModel
    private lateinit var musicListAdapter: MusicListAdapter
    private lateinit var exoPlayer: ExoPlayer
    private var musicList = arrayListOf<MusicDataClass>()
    private var position = 0

    private val storagePermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) showMusics() else permissionViewModel.requestPermission(this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        initializeViewModels()
        setupExoPlayer()
        requestStoragePermission()
        setupSwipeRefresh()
        setupMusicPlayButton()
    }

    private fun setupUI() {
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.darker_gray)
    }

    private fun initializeViewModels() {
        permissionViewModel = ViewModelProvider(this)[PermissionViewModel::class.java]
        viewModel = ViewModelProvider(this)[MusicListViewModel::class.java]
    }

    private fun setupExoPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.addListener(playerListener)
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.WRITE_EXTERNAL_STORAGE
        storagePermissionLauncher.launch(permission)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            showMusics()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupMusicPlayButton() {
        binding.songInRun.setOnClickListener {
            viewModel.viewMusicPlayLayout(this, binding, binding.layoutMusicPlay, musicList, position, musicListAdapter)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showMusics() {
        musicList = viewModel.fetchMedia(this)
        musicListAdapter = MusicListAdapter(this@MusicListActivity, musicList, exoPlayer,
            object : MusicListAdapter.OnClick {
                override fun playMusic(musicList: List<MusicDataClass>, position: Int) {
                    this@MusicListActivity.position = position
                    startMusic(position)
                }
            })
        musicListAdapter.notifyDataSetChanged()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MusicListActivity)
            adapter = musicListAdapter
        }
    }

    private fun startMusic(position: Int) {
        binding.songInRun.visibility = View.VISIBLE
        viewModel.startMusic(this, this, binding, position, exoPlayer, musicListAdapter)

        updatePlayPauseIcon(isPlaying = true)
        musicController()
    }

    private fun updatePlayPauseIcon(isPlaying: Boolean) {
        val iconRes = if (isPlaying) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24
        Glide.with(this).load(iconRes).into(binding.imgVPlayPause)
        Glide.with(this).load(iconRes).into(binding.layoutMusicPlay.playPause)
    }

    private fun musicController() {
        binding.imgVPlayPause.setOnClickListener {
            exoPlayer.playWhenReady = !exoPlayer.playWhenReady
            if (exoPlayer.playWhenReady) exoPlayer.play() else exoPlayer.pause()
            updatePlayPauseIcon(exoPlayer.playWhenReady)
        }

        binding.imgVNext.setOnClickListener { playNext() }
    }

    private fun playNext() {
        if (exoPlayer.isPlaying) {
            exoPlayer.stop()
            position = (position + 1) % musicList.size
            startMusic(position)
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            binding.txtCurrentSong.text = mediaItem?.mediaMetadata?.displayTitle
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) playNext()
        }
    }

    override fun onBackPressed() {
        if (binding.layoutMusicPlay.musicPlayScreen.visibility == View.VISIBLE) {
            binding.layoutMusicPlay.musicPlayScreen.visibility = View.GONE
            binding.layoutMusicPlayerHome.visibility = View.VISIBLE
            binding.homeToolbar.visibility = View.VISIBLE
            window.statusBarColor = ContextCompat.getColor(this, R.color.darker_gray)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoPlayer.isPlaying) exoPlayer.stop()
        exoPlayer.release()
    }
}