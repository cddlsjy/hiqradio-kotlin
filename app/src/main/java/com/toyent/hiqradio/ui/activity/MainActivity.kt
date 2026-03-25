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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.toyent.hiqradio.R
import com.toyent.hiqradio.data.model.Station
import com.toyent.hiqradio.data.model.ThemeMode
import com.toyent.hiqradio.data.repository.RadioRepository
import com.toyent.hiqradio.databinding.ActivityMainBinding
import com.toyent.hiqradio.service.AudioPlaybackService
import com.toyent.hiqradio.ui.fragment.FavoritesFragment
import com.toyent.hiqradio.ui.fragment.RecentlyPlayedFragment
import com.toyent.hiqradio.ui.fragment.StationsFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Main activity - phone home page
 */
class MainActivity : AppCompatActivity(), AudioPlaybackService.PlaybackListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: RadioRepository

    private var audioService: AudioPlaybackService? = null
    private var serviceBound = false

    private var currentPlayingStation: Station? = null
    private var isPlaying = false
    private var isBuffering = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlaybackService.AudioBinder
            audioService = binder.getService()
            audioService?.setPlaybackListener(this@MainActivity)
            serviceBound = true

            // Check for auto play
            if (repository.getAutoStart()) {
                val lastStation = repository.getLastPlayedStation()
                if (lastStation != null) {
                    playStation(lastStation)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize repository first
        repository = RadioRepository.getInstance(this)

        // Apply theme before super.onCreate()
        when (repository.getThemeMode()) {
            ThemeMode.LIGHT -> setTheme(R.style.Theme_HiqRadio)
            ThemeMode.DARK -> setTheme(R.style.Theme_HiqRadio_Dark)
            ThemeMode.SYSTEM -> setTheme(R.style.Theme_HiqRadio)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupTabs()
        setupBottomControls()

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

    private fun initViews() {
        // No toolbar
    }

    private fun setupTabs() {
        // Add fragments
        val fragments = listOf(
            StationsFragment(),
            FavoritesFragment(),
            RecentlyPlayedFragment()
        )

        val tabTitles = listOf(
            getString(R.string.nav_stations),
            getString(R.string.nav_favorites),
            getString(R.string.nav_recently)
        )

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tabTitles[0]))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tabTitles[1]))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tabTitles[2]))

        // Load default fragment
        loadFragment(StationsFragment())

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val fragment = when (tab?.position) {
                    0 -> StationsFragment()
                    1 -> FavoritesFragment()
                    2 -> RecentlyPlayedFragment()
                    else -> StationsFragment()
                }
                loadFragment(fragment)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setupBottomControls() {
        binding.playPauseButton.setOnClickListener {
            if (isPlaying) {
                pausePlayback()
            } else {
                if (currentPlayingStation != null) {
                    resumePlayback()
                }
            }
        }

        binding.playerBar.setOnClickListener {
            // Navigate to playing activity
            startActivity(Intent(this, PlayingActivity::class.java))
        }

        // Observe favorites count
        lifecycleScope.launch {
            repository.getAllFavorites().collectLatest { favorites ->
                // Update UI if needed
            }
        }
    }

    fun playStation(station: Station) {
        currentPlayingStation = station
        repository.saveLastPlayedStation(station)

        lifecycleScope.launch {
            repository.addToRecentlyPlayed(station)
        }

        audioService?.playStation(station)
        updatePlayerUI(true, false)
    }

    fun toggleFavorite(station: Station) {
        lifecycleScope.launch {
            if (repository.isFavorite(station.stationUuid)) {
                repository.removeFromFavorites(station.stationUuid)
                Toast.makeText(this@MainActivity, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show()
            } else {
                repository.addToFavorites(station)
                Toast.makeText(this@MainActivity, R.string.added_to_favorites, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pausePlayback() {
        audioService?.pause()
        updatePlayerUI(false, false)
    }

    private fun resumePlayback() {
        audioService?.play()
        updatePlayerUI(true, false)
    }

    private fun updatePlayerUI(playing: Boolean, buffering: Boolean) {
        isPlaying = playing
        isBuffering = buffering

        if (currentPlayingStation != null) {
            binding.playerBar.visibility = View.VISIBLE
            binding.stationName.text = currentPlayingStation?.name

            if (buffering) {
                binding.playPauseButton.setImageResource(android.R.drawable.ic_media_play)
            } else {
                binding.playPauseButton.setImageResource(
                    if (playing) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
            }
        } else {
            binding.playerBar.visibility = View.GONE
        }
    }

    // PlaybackListener implementation
    override fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean) {
        runOnUiThread {
            updatePlayerUI(isPlaying, isBuffering)
        }
    }

    override fun onError(error: String) {
        runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            updatePlayerUI(false, false)
        }
    }

    override fun onStationChanged(station: Station?) {
        runOnUiThread {
            currentPlayingStation = station
            if (station != null) {
                binding.playerBar.visibility = View.VISIBLE
                binding.stationName.text = station.name
            } else {
                binding.playerBar.visibility = View.GONE
            }
        }
    }
}
