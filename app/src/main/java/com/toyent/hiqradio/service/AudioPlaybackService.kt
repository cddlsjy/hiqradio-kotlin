package com.toyent.hiqradio.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.toyent.hiqradio.R
import com.toyent.hiqradio.data.model.Station
import com.toyent.hiqradio.ui.activity.MainActivity

/**
 * Audio playback service using ExoPlayer
 */
class AudioPlaybackService : Service() {

    private var exoPlayer: ExoPlayer? = null
    private var currentStation: Station? = null

    private val binder = AudioBinder()

    private var playbackListener: PlaybackListener? = null

    interface PlaybackListener {
        fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean)
        fun onError(error: String)
        fun onStationChanged(station: Station?)
    }

    inner class AudioBinder : Binder() {
        fun getService(): AudioPlaybackService = this@AudioPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializePlayer()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stop()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    private fun initializePlayer() {
        // Create HTTP data source factory with custom user agent
        val dataSourceFactory = DefaultHttpDataSource.Factory().apply {
            setUserAgent("HiqRadio/1.0")
            setConnectTimeoutMs(30000)
            setReadTimeoutMs(30000)
            setAllowCrossProtocolRedirects(true)
        }

        // Create media source factory
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        exoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            notifyPlaybackStateChanged(true, false)
                        }
                        Player.STATE_BUFFERING -> {
                            notifyPlaybackStateChanged(isPlaying, true)
                        }
                        Player.STATE_ENDED, Player.STATE_IDLE -> {
                            notifyPlaybackStateChanged(false, false)
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (!isPlaying && exoPlayer?.playbackState == Player.STATE_READY) {
                        notifyPlaybackStateChanged(false, false)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    val errorMessage = when {
                        error.message?.contains("network") == true || error.message?.contains("connection") == true -> "Network error: Unable to connect to the station"
                        error.message?.contains("not found") == true -> "Station URL not found"
                        error.message?.contains("unsupported") == true || error.message?.contains("format") == true -> "Unsupported audio format"
                        error.message?.contains("decoding") == true -> "Audio decoding error"
                        error.message?.contains("timeout") == true -> "Connection timeout"
                        error.message?.contains("SSL") == true || error.message?.contains("certificate") == true -> "SSL certificate error"
                        else -> "Playback error: ${error.message ?: "Unknown error"}"
                    }
                    notifyError(errorMessage)
                }
            })
        }
    }

    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    fun playStation(station: Station) {
        currentStation = station

        val url = station.urlResolved
        if (url.isEmpty()) {
            val errorMessage = "Station URL is empty"
            notifyError(errorMessage)
            return
        }

        try {
            exoPlayer?.apply {
                val mediaItem = MediaItem.fromUri(url)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }

            playbackListener?.onStationChanged(station)
            startForeground(NOTIFICATION_ID, createNotification(station))
        } catch (e: Exception) {
            val errorMessage = "Error playing station: ${e.message ?: "Unknown error"}"
            notifyError(errorMessage)
        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun stop() {
        exoPlayer?.stop()
        currentStation = null
        playbackListener?.onStationChanged(null)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying == true
    }

    fun isBuffering(): Boolean {
        return exoPlayer?.playbackState == Player.STATE_BUFFERING
    }

    fun getCurrentStation(): Station? {
        return currentStation
    }

    fun setPlaybackListener(listener: PlaybackListener?) {
        playbackListener = listener
    }

    private fun notifyPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean) {
        playbackListener?.onPlaybackStateChanged(isPlaying, isBuffering)
    }

    private fun notifyError(error: String) {
        playbackListener?.onError(error)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Radio playback controls"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(station: Station): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(station.name)
            .setContentText(station.country ?: "Radio Station")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "hiqradio_playback"
        const val NOTIFICATION_ID = 1

        const val ACTION_PLAY = "com.toyent.hiqradio.ACTION_PLAY"
        const val ACTION_PAUSE = "com.toyent.hiqradio.ACTION_PAUSE"
        const val ACTION_STOP = "com.toyent.hiqradio.ACTION_STOP"
    }
}
