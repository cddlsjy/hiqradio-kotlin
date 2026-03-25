package com.toyent.hiqradio.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.toyent.hiqradio.R
import com.toyent.hiqradio.data.model.Station
import com.toyent.hiqradio.data.repository.RadioRepository
import com.toyent.hiqradio.databinding.ActivityPlayingBinding
import com.toyent.hiqradio.service.AudioPlaybackService

/**
 * Activity displaying full screen player
 */
class PlayingActivity : AppCompatActivity(), AudioPlaybackService.PlaybackListener {

    private lateinit var binding: ActivityPlayingBinding
    private lateinit var repository: RadioRepository

    private var audioService: AudioPlaybackService? = null
    private var serviceBound = false

    private var currentStation: Station? = null
    private var isPlaying = false
    private var isBuffering = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlaybackService.AudioBinder
            audioService = binder.getService()
            audioService?.setPlaybackListener(this@PlayingActivity)
            serviceBound = true
            updateUI()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = RadioRepository.getInstance(this)

        setupViews()

        // Bind to audio service
        Intent(this, AudioPlaybackService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        if (serviceBound) {
            audioService?.setPlaybackListener(null)
            unbindService(serviceConnection)
            serviceBound = false
        }
        super.onDestroy()
    }

    private fun setupViews() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.playPauseButton.setOnClickListener {
            if (isPlaying) {
                audioService?.pause()
            } else {
                audioService?.play()
            }
        }

        binding.stopButton.setOnClickListener {
            audioService?.stop()
            finish()
        }
    }

    private fun updateUI() {
        currentStation = audioService?.getCurrentStation()

        if (currentStation != null) {
            binding.stationName.text = currentStation?.name
            binding.stationCountry.text = currentStation?.country ?: ""
            binding.stationTags.text = currentStation?.tags ?: ""

            // Load icon
            if (!currentStation?.favicon.isNullOrEmpty()) {
                binding.stationIcon.load(currentStation?.favicon) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_foreground)
                    error(R.drawable.ic_launcher_foreground)
                    transformations(CircleCropTransformation())
                }
            }

            // Bitrate
            if (currentStation?.bitrate != null && currentStation?.bitrate!! > 0) {
                binding.stationBitrate.text = "${currentStation?.bitrate} kbps"
            }

            // Codec
            if (!currentStation?.codec.isNullOrEmpty()) {
                binding.stationCodec.text = currentStation?.codec
            }
        }

        updatePlaybackState()
    }

    private fun updatePlaybackState() {
        isPlaying = audioService?.isPlaying() == true
        isBuffering = audioService?.isBuffering() == true

        if (isBuffering) {
            binding.playPauseButton.isEnabled = false
            binding.statusText.text = getString(R.string.buffering)
        } else {
            binding.playPauseButton.isEnabled = true
            binding.statusText.text = if (isPlaying) getString(R.string.play) else getString(R.string.stop)
        }

        binding.playPauseButton.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
    }

    // PlaybackListener implementation
    override fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean) {
        runOnUiThread {
            this.isPlaying = isPlaying
            this.isBuffering = isBuffering
            updatePlaybackState()
        }
    }

    override fun onError(error: String) {
        runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    override fun onStationChanged(station: Station?) {
        runOnUiThread {
            currentStation = station
            updateUI()
        }
    }
}
