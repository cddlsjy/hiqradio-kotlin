package com.toyent.hiqradio.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.toyent.hiqradio.R
import com.toyent.hiqradio.data.model.Station
import com.toyent.hiqradio.databinding.ItemStationBinding

/**
 * RecyclerView adapter for displaying stations
 */
class StationAdapter(
    private val stations: List<Station>,
    private val onItemClick: (Station) -> Unit,
    private val onFavoriteClick: (Station) -> Unit
) : RecyclerView.Adapter<StationAdapter.StationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val binding = ItemStationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        holder.bind(stations[position])
    }

    override fun getItemCount(): Int = stations.size

    inner class StationViewHolder(
        private val binding: ItemStationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(station: Station) {
            binding.apply {
                stationName.text = station.name
                stationCountry.text = station.country ?: ""
                stationTags.text = station.tags ?: ""

                // Load station icon
                if (!station.favicon.isNullOrEmpty()) {
                    stationIcon.load(station.favicon) {
                        crossfade(true)
                        placeholder(R.drawable.ic_launcher_foreground)
                        error(R.drawable.ic_launcher_foreground)
                        transformations(CircleCropTransformation())
                    }
                } else {
                    stationIcon.setImageResource(R.drawable.ic_launcher_foreground)
                }

                // Bitrate
                if (station.bitrate != null && station.bitrate > 0) {
                    stationBitrate.text = "${station.bitrate} kbps"
                } else {
                    stationBitrate.text = ""
                }

                // Click listeners
                root.setOnClickListener {
                    onItemClick(station)
                }

                favoriteButton.setOnClickListener {
                    onFavoriteClick(station)
                }
            }
        }
    }
}
