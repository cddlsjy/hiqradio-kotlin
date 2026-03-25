package com.toyent.hiqradio.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.toyent.hiqradio.R
import com.toyent.hiqradio.data.model.RecentlyPlayed
import com.toyent.hiqradio.databinding.ItemRecentlyPlayedBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView adapter for displaying recently played stations
 */
class RecentlyPlayedAdapter(
    private val recentlyPlayed: List<RecentlyPlayed>,
    private val onItemClick: (RecentlyPlayed) -> Unit
) : RecyclerView.Adapter<RecentlyPlayedAdapter.RecentlyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentlyViewHolder {
        val binding = ItemRecentlyPlayedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecentlyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentlyViewHolder, position: Int) {
        holder.bind(recentlyPlayed[position])
    }

    override fun getItemCount(): Int = recentlyPlayed.size

    inner class RecentlyViewHolder(
        private val binding: ItemRecentlyPlayedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recently: RecentlyPlayed) {
            binding.apply {
                stationName.text = recently.name
                stationCountry.text = recently.country ?: ""

                // Format played time
                val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                playedTime.text = dateFormat.format(Date(recently.playedTime))

                // Load station icon
                if (!recently.favicon.isNullOrEmpty()) {
                    stationIcon.load(recently.favicon) {
                        crossfade(true)
                        placeholder(R.drawable.ic_launcher_foreground)
                        error(R.drawable.ic_launcher_foreground)
                        transformations(CircleCropTransformation())
                    }
                } else {
                    stationIcon.setImageResource(R.drawable.ic_launcher_foreground)
                }

                // Click listener
                root.setOnClickListener {
                    onItemClick(recently)
                }
            }
        }
    }
}
