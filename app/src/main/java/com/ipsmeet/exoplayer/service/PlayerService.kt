package com.ipsmeet.exoplayer.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.Binder
import android.os.IBinder
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.ui.PlayerNotificationManager
import com.ipsmeet.exoplayer.R
import com.ipsmeet.exoplayer.activity.MusicListActivity


@UnstableApi class PlayerService : Service() {

    private val iBinder = ServiceBinder()
    var player: Player? = null
    private var mediaSession: MediaSession? = null
    lateinit var notificationManager: PlayerNotificationManager
    private lateinit var nBuilder: NotificationCompat.Builder

    inner class ServiceBinder : Binder() {
        fun getPlayerService(): PlayerService = this@PlayerService
    }

    override fun onBind(intent: Intent?): IBinder {
        return iBinder
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(pendingIntent()!!)
            /*.setId(Random(5).toString())*/
            .build()

        notificationManager = PlayerNotificationManager.Builder(this, 111, R.string.notification_channel_name.toString())
            .setChannelImportance(IMPORTANCE_HIGH)
            .setSmallIconResourceId(R.drawable.round_music_note_24)
            .setChannelDescriptionResourceId(R.string.app_name)
            .setChannelNameResourceId(R.string.app_name)
            .setMediaDescriptionAdapter(audioDescriptor)
            .setNotificationListener(notificationListener)
            .setNextActionIconResourceId(R.drawable.round_skip_next_24)
            .setPreviousActionIconResourceId(R.drawable.round_skip_previous_24)
            .setPlayActionIconResourceId(R.drawable.round_play_arrow_24)
            .setPauseActionIconResourceId(R.drawable.round_pause_24)
            /*.setCustomActionReceiver(object : PlayerNotificationManager.CustomActionReceiver {
                override fun createCustomActions(context: Context, instanceId: Int): MutableMap<String, NotificationCompat.Action> {
                    val prev: NotificationCompat.Action = NotificationCompat.Action(
                        R.drawable.round_skip_previous_24,
                        ACTION_PREVIOUS,
                        pendingIntent()
                    )
                }

                override fun getCustomActions(player: Player): MutableList<String> {

                }

                override fun onCustomAction(player: Player, action: String, intent: Intent) {
                    Log.d("action",  action)
                }
            })*/
            .build()

        notificationManager.apply {
            setPlayer(player)
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
            setUseRewindAction(false)
            setUseFastForwardAction(false)
            setUseNextAction(true)
            setUsePreviousAction(true)
            setUseChronometer(true)
        }
    }

    private val notificationListener = object : PlayerNotificationManager.NotificationListener {
        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            super.onNotificationCancelled(notificationId, dismissedByUser)
            stopForeground(true)
            if (player?.isPlaying!!) {
                player?.stop()
                player?.release()
            }
            stopSelf()
        }

        override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
            super.onNotificationPosted(notificationId, notification, ongoing)
            startForeground(notificationId, notification)
        }
    }

    private val audioDescriptor = object : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return player.currentMediaItem?.mediaMetadata?.albumTitle!!
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return pendingIntent()
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return null
        }

        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap {
            val imageView = ImageView(applicationContext)
            imageView.setImageURI(player.currentMediaItem?.mediaMetadata?.artworkUri)

            var bitmapDrawable = imageView.drawable
            if (bitmapDrawable == null) {
                bitmapDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.round_music_note_24)
            }

            return  bitmapDrawable.toBitmap()
        }
    }

    private fun pendingIntent(): PendingIntent? {
        val intent = Intent(applicationContext, MusicListActivity::class.java)
        return PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onDestroy() {
        if (player?.isPlaying!!) {
            player?.stop()
        }
        notificationManager.setPlayer(null)
        player?.release()
        player = null
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }
}