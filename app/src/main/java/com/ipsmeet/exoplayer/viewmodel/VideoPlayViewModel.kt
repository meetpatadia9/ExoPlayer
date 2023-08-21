package com.ipsmeet.exoplayer.viewmodel

import android.app.Activity
import android.content.pm.ActivityInfo
import android.media.MediaFormat
import android.os.Handler
import android.view.WindowManager
import androidx.lifecycle.ViewModel
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.*
import androidx.media3.exoplayer.video.VideoFrameMetadataListener
import com.google.common.net.HttpHeaders
import com.ipsmeet.exoplayer.dataclass.MediaFileDataClass

@UnstableApi class VideoPlayViewModel: ViewModel() {

    private lateinit var exoPlayer: ExoPlayer

    fun setFullScreen(context: Activity) {
        context.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    fun playVideo(context: Activity, videoPlayList: ArrayList<MediaFileDataClass>, position: Int, exoPlayer: ExoPlayer) {
        val mediaItem = MediaItem.fromUri(videoPlayList[position].path!!)
        val dataSourceFactory = DefaultDataSourceFactory(context, HttpHeaders.USER_AGENT)
        val concatenatingMediaSource = ConcatenatingMediaSource()

        var currentOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        // listen for video size changes
        val videoListener = @UnstableApi object : VideoFrameMetadataListener {
            override fun onVideoFrameAboutToBeRendered(presentationTimeUs: Long, releaseTimeNs: Long, format: Format, mediaFormat: MediaFormat?) {
                val newOrientation = if (format.width > format.height) {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }

                // update the activity's orientation if it has changed
                if (newOrientation != currentOrientation) {
                    currentOrientation = newOrientation
                    context.requestedOrientation = currentOrientation
                }
            }
        }

        for (i in 0..videoPlayList.size) {
            val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            concatenatingMediaSource.addMediaSource(mediaSourceFactory)
        }

        concatenatingMediaSource.addEventListener(Handler(), object : MediaSourceEventListener {
            override fun onLoadCompleted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
                super.onLoadCompleted(windowIndex, mediaPeriodId, loadEventInfo, mediaLoadData)
                if (mediaLoadData.trackFormat != null) {
                    context.requestedOrientation = if (mediaLoadData.trackFormat!!.width > mediaLoadData.trackFormat!!.height) {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }
            }
        })

        exoPlayer.apply {
            prepare(concatenatingMediaSource)
            play()
            playWhenReady = true
            seekTo(position, C.TIME_UNSET)
            setVideoFrameMetadataListener(videoListener)
        }
    }

}