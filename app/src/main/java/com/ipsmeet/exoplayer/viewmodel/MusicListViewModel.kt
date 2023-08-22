package com.ipsmeet.exoplayer.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.*
import com.bumptech.glide.Glide
import com.google.common.net.HttpHeaders
import com.ipsmeet.exoplayer.R
import com.ipsmeet.exoplayer.adapter.MusicListAdapter
import com.ipsmeet.exoplayer.databinding.ActivityMusicListBinding
import com.ipsmeet.exoplayer.databinding.LayoutMusicPlayBinding
import com.ipsmeet.exoplayer.dataclass.MusicDataClass
import java.util.concurrent.TimeUnit

@UnstableApi class MusicListViewModel: ViewModel() {

    lateinit var exo: ExoPlayer
    private var musicFiles = arrayListOf<MusicDataClass>()
    var pos = 0

    @SuppressLint("Range")
    fun fetchMedia(context: Context): ArrayList<MusicDataClass> {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${ MediaStore.Audio.Media.IS_MUSIC } != 0 AND ${ MediaStore.Audio.Media.MIME_TYPE } = 'audio/mpeg'"    // selection for mp3 only
        val sortOrder = "${ MediaStore.Audio.Media.DISPLAY_NAME } ASC"    // ascending order by title
        val cursor = context.contentResolver.query(uri, null, selection, null, sortOrder)

        cursor?.use {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                val albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                val size = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE))
                val duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED))

                val mediaData = MusicDataClass(id, title, albumId, displayName, size, duration, path, dateAdded, false)
                musicFiles.add(mediaData)
            }
        }

        return musicFiles
    }

    fun getCurrentPosition(): Int {
        return pos
    }

    @SuppressLint("NotifyDataSetChanged")
    fun startMusic(context: Context, activity: Activity, binding: ActivityMusicListBinding, position: Int, exoPlayer: ExoPlayer, musicListAdapter: MusicListAdapter) {
        /*if (position > pos) {
            musicFiles[position-1].isPlaying = false
        } else {
            musicFiles[position+1].isPlaying = false
        }*/
        musicFiles[pos].isPlaying = false
        musicFiles[position].isPlaying = true
        musicListAdapter.notifyDataSetChanged()

        exo = exoPlayer
        pos = position

        val mediaMetaData = MediaMetadata.Builder()
            .setAlbumTitle(musicFiles[position].displayName)
            .setArtworkUri(Uri.parse("content://media/external/audio/albumart/${ musicFiles[position].albumId!!.toLong() }"))
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaMetadata(mediaMetaData)
            .setUri(musicFiles[position].path!!)

        exo.setMediaItem(mediaItem)
        val dataSourceFactory = DefaultDataSourceFactory(activity, HttpHeaders.USER_AGENT)
        val concatenatingMediaSource = ConcatenatingMediaSource()

        /*val mediaMetaData = MediaMetadata.Builder()
            .setAlbumTitle(MusicPlayerData.audioTitle)
            .build()
        val mediaItem =
            MediaItem.Builder().setMediaMetadata(mediaMetaData)
                .setUri(MusicPlayerData.audioUri)
                .build()
        player?.setMediaItem(mediaItem)*/

        for (i in 0..musicFiles.size) {
            val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            concatenatingMediaSource.addMediaSource(mediaSourceFactory)
        }

        /*concatenatingMediaSource.addEventListener(Handler(), object : MediaSourceEventListener {
            override fun onLoadCompleted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
                super.onLoadCompleted(windowIndex, mediaPeriodId, loadEventInfo, mediaLoadData)
                if (mediaLoadData.trackFormat != null) {
                    activity.requestedOrientation = if (mediaLoadData.trackFormat!!.width > mediaLoadData.trackFormat!!.height) {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }
            }
        })*/

        exo.apply {
            prepare()
            play()
            playWhenReady = true
            seekTo(position, C.TIME_UNSET)

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_ENDED) {
                        // Logic to play the next song
//                        musicFiles[position].isPlaying = false
                        pos++
                        if (pos > musicFiles.size-1) {
                            pos = 0
                        } else {
                            pos
                        }

                        binding.layoutMusicPlay.txtMusicProgress.text = displayTime(00.00)
                        binding.layoutMusicPlay.musicSeekBar.progress = 0
                        startMusic(activity.applicationContext, activity, binding, pos, exo, musicListAdapter)

                        exo.setMediaItem(mediaItem)
                        val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)
                        concatenatingMediaSource.addMediaSource(mediaSourceFactory)

                        exo.prepare()
                        exo.playWhenReady = true
                    }
                }
            })
        }

        val albumArt = Uri.parse("content://media/external/audio/albumart/${ musicFiles[position].albumId!!.toLong() }")
        Glide.with(context).load(albumArt).placeholder(R.drawable.img_boy_listening).into(binding.imgVMusicThumbnail)
        Glide.with(context).load(albumArt).placeholder(R.drawable.img_boy_listening).into(binding.layoutMusicPlay.imgVAlbumArt)
        binding.txtCurrentSong.text = musicFiles[position].displayName
        binding.layoutMusicPlay.txtMusicName.text = musicFiles[position].displayName
        Glide.with(context).load(R.drawable.round_pause_24).into(binding.layoutMusicPlay.playPause)
        //  music duration in player-view
        binding.layoutMusicPlay.txtMusicDuration.text = displayTime(musicFiles[position].duration!!.toDouble())
    }

    fun ExoPlayer.setMediaItem(builder: MediaItem.Builder) {
        val mediaItem = builder.build()
        setMediaItem(mediaItem)
    }

    fun ProgressiveMediaSource.Factory.createMediaSource(builder: MediaItem.Builder): ProgressiveMediaSource {
        val mediaItem = builder.build()
        return createMediaSource(mediaItem)
    }

    //  MUSIC-PLAY VIEW
    fun viewMusicPlayLayout(activity: Activity, binding: ActivityMusicListBinding, layoutMusicPlay: LayoutMusicPlayBinding, musicList: ArrayList<MusicDataClass>, position: Int, musicListAdapter: MusicListAdapter) {
        pos = position
        binding.homeToolbar.visibility = View.GONE
        binding.layoutMusicPlayerHome.visibility = View.GONE
        activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.darkest_gray)
        layoutMusicPlay.apply {
            //  visible layout
            musicPlayScreen.visibility = View.VISIBLE

            //  hide layout
            imgVBack.setOnClickListener {
                musicPlayScreen.visibility = View.GONE
                binding.homeToolbar.visibility = View.VISIBLE
                binding.layoutMusicPlayerHome.visibility = View.VISIBLE
                activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.darker_gray)
            }

            //  open play-list
            imgVPlaylist.setOnClickListener {
                musicPlayScreen.visibility = View.GONE
                binding.layoutMusicPlayerHome.visibility = View.VISIBLE
            }

            //  max progress of seekbar
            musicSeekBar.max = musicList[pos].duration!!.toInt()

            val handler = Handler()
            handler.postDelayed(object : Runnable {
                override fun run() {
                    try {
                        handler.postDelayed(this, 0)
                        //  current-progressed time
                        txtMusicProgress.text = displayTime(exo.currentPosition.toDouble())
                        //  current-progressed seekbar
                        musicSeekBar.progress = exo.currentPosition.toInt()

                        //  if current-music completes, then play next song
                        if (displayTime(exo.currentPosition.toDouble()).equals(displayTime(musicList[pos].duration!!.toDouble()))) {
                            if (exo.isPlaying)
                                exo.pause()

//                            musicFiles[position].isPlaying = false
                            pos++
                            if (pos > musicList.size-1) {
                                pos = 0
                            } else {
                                pos
                            }

                            txtMusicProgress.text = displayTime(00.00)
                            musicSeekBar.progress = 0
                            getCurrentPosition()
                            startMusic(activity.applicationContext, activity, binding, pos, exo, musicListAdapter)
                        }
                    }
                    catch (e: Exception) {
                        txtMusicProgress.text = displayTime(00.00)
                        musicSeekBar.progress = 0
                    }
                }
            }, 1000)

            //  setOnSeekBarChangeListener
            seekbarEvent(musicSeekBar, exo)

            //  MUSIC CONTROLS
            playPause.setOnClickListener {
                if (exo.playWhenReady) {
                    exo.playWhenReady = false
                    exo.pause()
                    Glide.with(activity.applicationContext).load(R.drawable.round_play_arrow_24).into(playPause)
                    Glide.with(activity.applicationContext).load(R.drawable.round_play_arrow_24).into(binding.imgVPlayPause)
                }
                else {
                    exo.playWhenReady = true
                    exo.play()
                    Glide.with(activity.applicationContext).load(R.drawable.round_pause_24).into(playPause)
                    Glide.with(activity.applicationContext).load(R.drawable.round_pause_24).into(binding.imgVPlayPause)
                }
            }

            playNext.setOnClickListener {
                if (exo.isPlaying) {
                    exo.pause()
                }

//                musicFiles[position].isPlaying = false
                pos++

                if (pos > musicList.size-1) {
                    pos = 0

                } else {
                    pos
                }

                txtMusicProgress.text = displayTime(00.00)
                musicSeekBar.progress = 0
                getCurrentPosition()
                startMusic(activity.applicationContext, activity, binding, pos, exo, musicListAdapter)
            }

            playPrevious.setOnClickListener {
                if (exo.isPlaying) {
                    exo.pause()
                }

//                musicFiles[position].isPlaying = false
                pos--

                if (pos < 0) {
                    pos = musicList.size-1
                } else {
                    pos
                }

                txtMusicProgress.text = displayTime(00.00)
                musicSeekBar.progress = 0
                getCurrentPosition()
                startMusic(activity.applicationContext, activity, binding, pos, exo, musicListAdapter)
            }
        }
    }

    //  CONVERT MILLISECONDS IN TO `Minute:Second`
    fun displayTime(millisecond: Double): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millisecond.toLong())
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisecond.toLong()) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisecond.toLong()))
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisecond.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisecond.toLong()))

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun seekbarEvent(musicSeekBar: SeekBar, exoPlayer: ExoPlayer) {
        musicSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var newProgress = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                newProgress = seekBar!!.progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar!!.progress = newProgress
                musicSeekBar.progress = newProgress
                exoPlayer.seekTo(newProgress.toLong())
            }
        })
    }

}
