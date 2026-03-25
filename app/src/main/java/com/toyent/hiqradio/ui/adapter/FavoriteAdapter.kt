package com.toyent.hiqradio.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.toyent.hiqradio.R
import com.toyent.hiqradio.data.model.FavoriteStation
import com.toyent.hiqradio.databinding.ItemFavoriteBinding

/**
 * RecyclerView adapter for displaying favorite stations
 */
class FavoriteAdapter(
    private val favorites: List<FavoriteStation>,
    private val onItemClick: (FavoriteStation) -> Unit,
    private val onRemoveClick: (FavoriteStation) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favorites[position])
    }

    override fun getItemCount(): Int = favorites.size

    inner class FavoriteViewHolder(
        private val binding: ItemFavoriteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(favorite: FavoriteStation) {
            binding.apply {
                stationName.text = favorite.name
                stationCountry.text = favorite.country ?: ""

                // Load station icon
                if (!favorite.favicon.isNullOrEmpty()) {
                    stationIcon.load(favorite.favicon) {
                        crossfade(true)
                        placeholder(R.drawable.ic_launcher_foreground)
                        error(R.drawable.ic_launcher_foreground)
                        transformations(CircleCropTransformation())
                    }
                } else {
                    stationIcon.setImageResource(R.drawable.ic_launcher_foreground)
                }

                // Click listeners
                root.setOnClickListener {
                    onItemClick(favorite)
                }

                removeButton.setOnClickListener {
                    onRemoveClick(favorite)
                }
            }
        }
    }
}
