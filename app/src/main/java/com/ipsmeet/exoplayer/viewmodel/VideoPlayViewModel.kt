package com.ipsmeet.exoplayer.viewmodel

import android.app.Activity
import android.content.pm.ActivityInfo
import android.media.MediaFormat
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MediaSourceEventListener
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.video.VideoFrameMetadataListener
import com.google.common.net.HttpHeaders
import com.ipsmeet.exoplayer.R
import com.ipsmeet.exoplayer.dataclass.MediaFileDataClass
import com.ipsmeet.exoplayer.viewmodel.SuperGlobal.currentPos

@UnstableApi class VideoPlayViewModel: ViewModel() {

    private lateinit var exoPlayer: ExoPlayer
//    val currentPositionLiveData = MutableLiveData<Int>()

    fun setFullScreen(context: Activity) {
        context.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    fun playVideo(context: Activity, videoPlayList: ArrayList<MediaFileDataClass>, position: Int, exoPlayer: ExoPlayer) {
        currentPos = position

        Log.i("playVideo: position",  position.toString())
        Log.i("playVideo: currentPos",  currentPos.toString())

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

//        currentPositionLiveData.value = currentPos

        exoPlayer.apply {
            prepare(concatenatingMediaSource)
            play()
            playWhenReady = true
            seekTo(position, C.TIME_UNSET)
            setVideoFrameMetadataListener(videoListener)
            addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_ENDED) {

                        Log.i("onPlaybackStateChanged: currentPos", currentPos.toString())

                        val nextPos = currentPos + 1
                        currentPos = nextPos
                        Log.i("onPlaybackStateChanged: currentPos++", nextPos.toString())


                        playVideo(context, videoPlayList, nextPos, exoPlayer)
                        /*val nextPosition = currentPosition + 1
                        if (nextPosition < videoPlayList.size) {
                            currentPos = nextPosition.toInt()
                            currentPositionLiveData.value = currentPos
                            val nextMediaItem = MediaItem.fromUri(videoPlayList[currentPos].path!!)
                            exoPlayer.setMediaItem(nextMediaItem)
                            exoPlayer.prepare()
                            exoPlayer.play()
                        }*/
                    }
                }
            })
        }
    }

    /*fun videoListener() {
        exoPlayer.
    }*/

}