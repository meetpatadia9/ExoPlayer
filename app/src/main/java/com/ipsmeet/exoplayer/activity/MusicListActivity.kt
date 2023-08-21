package com.ipsmeet.exoplayer.activity

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.common.util.concurrent.MoreExecutors
import com.ipsmeet.exoplayer.R
import com.ipsmeet.exoplayer.adapter.MusicListAdapter
import com.ipsmeet.exoplayer.databinding.ActivityMusicListBinding
import com.ipsmeet.exoplayer.dataclass.MusicDataClass
import com.ipsmeet.exoplayer.service.PlayerService
import com.ipsmeet.exoplayer.viewmodel.MusicListViewModel
import com.ipsmeet.exoplayer.viewmodel.PermissionViewModel

@UnstableApi class MusicListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMusicListBinding

    private lateinit var permissionViewModel: PermissionViewModel
    private lateinit var viewModel: MusicListViewModel

    private var musicList = arrayListOf<MusicDataClass>()
    private lateinit var exoPlayer: ExoPlayer
    var position = 0

    var isBound = false
    lateinit var sessionToken: SessionToken

    override fun onStart() {
        super.onStart()
        sessionToken = SessionToken(this, ComponentName(this, PlayerService::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.title = "Music Player"

        //  Initialize view-model
        permissionViewModel = ViewModelProvider(this)[PermissionViewModel::class.java]
        viewModel = ViewModelProvider(this)[MusicListViewModel::class.java]

        //  Initialize exo-player
        exoPlayer = ExoPlayer.Builder(this).build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        //  SwipeRefreshLayout - OnRefresh
        binding.swipeRefresh.setOnRefreshListener {
            showMusics()
            binding.swipeRefresh.isRefreshing = false
        }

        //  Open MusicPlay screen
        binding.songInRun.setOnClickListener {
            viewModel.viewMusicPlayLayout(this, binding, binding.layoutMusicPlay, musicList, position)
        }

        doServiceBinding()
    }

    private val storagePermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                showMusics()
            } else {
                permissionViewModel.requestPermission(this)
            }
        }

    private fun showMusics() {
        musicList = viewModel.fetchMedia(this@MusicListActivity)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MusicListActivity, LinearLayoutManager.VERTICAL, false)
            adapter = MusicListAdapter(this@MusicListActivity, musicList, exoPlayer,
            object : MusicListAdapter.OnClick {
                override fun playMusic(musicList: List<MusicDataClass>, position: Int) {
                    this@MusicListActivity.position = position
                    startMusic(position)
                }
            })
        }
    }

    private fun startMusic(position: Int) {
        binding.songInRun.visibility = View.VISIBLE
        viewModel.startMusic(this@MusicListActivity, this, binding, position, exoPlayer)

        Glide.with(this).load(R.drawable.round_pause_24).into(binding.imgVPlayPause)
        Glide.with(this).load(R.drawable.round_pause_24).into(binding.layoutMusicPlay.playPause)
        musicController()
    }

    //  Home music controller
    private fun musicController() {
        binding.imgVPlayPause.setOnClickListener {
            if (exoPlayer.playWhenReady) {
                exoPlayer.playWhenReady = false
                exoPlayer.pause()
                Glide.with(this).load(R.drawable.round_play_arrow_24).into(binding.imgVPlayPause)
                Glide.with(this).load(R.drawable.round_play_arrow_24).into(binding.layoutMusicPlay.playPause)
            }
            else {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
                Glide.with(this).load(R.drawable.round_pause_24).into(binding.imgVPlayPause)
                Glide.with(this).load(R.drawable.round_pause_24).into(binding.layoutMusicPlay.playPause)
            }
        }

        binding.imgVNext.setOnClickListener {
            if (exoPlayer.isPlaying) {
                if (exoPlayer.isPlaying) {
                    exoPlayer.stop()
                }

                if (position == viewModel.getCurrentPosition()) {
                    position
                } else {
                    position = viewModel.getCurrentPosition()
                }

                position++
                startMusic(position)
            }
        }
        audioListener()
    }

    private fun audioListener() {
        exoPlayer.addListener(object : Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                binding.txtCurrentSong.text = mediaItem!!.mediaMetadata.displayTitle
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                //  if current-music completes, then play next song
                if (exoPlayer.playbackState == Player.STATE_ENDED) {
                    position++
                    if (position > musicList.size-1) {
                        position = 0
                    } else {
                        position
                    }

                    binding.layoutMusicPlay.txtMusicProgress.text = viewModel.displayTime(00.00)
                    binding.layoutMusicPlay.musicSeekBar.progress = 0
                    startMusic(position)
                }
            }
        })
    }

    private fun doServiceBinding() {
        val intent = Intent(this, PlayerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        isBound = false
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: PlayerService.ServiceBinder = service as PlayerService.ServiceBinder
            exoPlayer = binder.getPlayerService().player as ExoPlayer
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    /*private fun setPlayerControls() {
//        binding.playerView.player = player
//        musicPlayerBinding.controls.player = player
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
//                    Player.STATE_BUFFERING -> show(musicPlayerBinding.progressCircular)
                    Player.STATE_READY -> {
                        exoPlayer?.playWhenReady = true
//                        hide(musicPlayerBinding.progressCircular)
                    }
                }
            }
        })
    }*/

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.layoutMusicPlay.musicPlayScreen.visibility == View.VISIBLE) {
            binding.layoutMusicPlay.musicPlayScreen.visibility = View.GONE
            binding.layoutMusicPlayerHome.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoPlayer.isPlaying)
            exoPlayer.stop()

        exoPlayer.release()
        unbindService(serviceConnection)
        isBound = false
    }
}