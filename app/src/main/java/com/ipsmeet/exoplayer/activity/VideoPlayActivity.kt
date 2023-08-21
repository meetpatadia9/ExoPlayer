package com.ipsmeet.exoplayer.activity

import android.os.Bundle
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.ipsmeet.exoplayer.R
import com.ipsmeet.exoplayer.databinding.ActivityVideoPlayBinding
import com.ipsmeet.exoplayer.dataclass.MediaFileDataClass
import com.ipsmeet.exoplayer.viewmodel.VideoPlayViewModel

@UnstableApi
class VideoPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayBinding

    private lateinit var viewModel: VideoPlayViewModel

    private lateinit var exoPlayer: ExoPlayer
    private var position = 0
    private lateinit var title: String
    private var videoPlayList = arrayListOf<MediaFileDataClass>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[VideoPlayViewModel::class.java]
        viewModel.setFullScreen(this)

        position = intent.getIntExtra("position", 1)
        title = intent.getStringExtra("displayName").toString()
        val bundle = intent.getBundleExtra("bundle")!!  // we have passed bundle, so we need to fetch it as bundle first
        videoPlayList = bundle.getParcelableArrayList("videoPlayList")!!    // bundle contains ArrayList

        // setting title of video in `custom_control_layout.xml
        findViewById<TextView>(R.id.txt_videoTitle).text = title
        initializeVideoPlayer()
    }

    private fun initializeVideoPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.playerView.apply {
            player = exoPlayer
            keepScreenOn = true
        }
        viewModel.playVideo(this, videoPlayList, position, exoPlayer)
        videoController()
    }

    private fun videoController() {
        //  PLAY-PAUSE
        findViewById<ImageView>(R.id.imgV_playPause).setOnClickListener {
            if (exoPlayer.playWhenReady) {
                exoPlayer.playWhenReady = false
                exoPlayer.pause()
                Glide.with(this).load(R.drawable.round_play_arrow_24)
                    .into(findViewById(R.id.imgV_playPause))
            } else {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
                Glide.with(this).load(R.drawable.round_pause_24)
                    .into(findViewById(R.id.imgV_playPause))
            }
        }

        //  NEXT
        findViewById<ImageView>(R.id.imgV_next).setOnClickListener {
            try {
                exoPlayer.stop()
                position++
                findViewById<TextView>(R.id.txt_videoTitle).text = videoPlayList[position].displayName
                initializeVideoPlayer()
            }
            catch (e: Exception) {
                finish()
            }
        }

        //  PREVIOUS
        findViewById<ImageView>(R.id.imgV_previous).setOnClickListener {
            try {
                exoPlayer.stop()
                position--
                findViewById<TextView>(R.id.txt_videoTitle).text = videoPlayList[position].displayName
                initializeVideoPlayer()
            }
            catch (e: Exception) {
                finish()
            }
        }

        //  FORWARD
        findViewById<ImageView>(R.id.imgV_forward).setOnClickListener {
            exoPlayer.seekTo(exoPlayer.currentPosition + 5000)
        }

        //  REPLAY
        findViewById<ImageView>(R.id.imgV_replay).setOnClickListener {
            exoPlayer.seekTo(exoPlayer.currentPosition - 5000)
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.apply {
            playWhenReady = false
            pause()
            playbackState
        }
    }

    override fun onRestart() {
        super.onRestart()
        exoPlayer.apply {
            playWhenReady = true
            playbackState
        }
    }

}