package com.toyent.hiqradio.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.toyent.hiqradio.data.model.FavoriteStation
import com.toyent.hiqradio.data.model.Station
import com.toyent.hiqradio.data.repository.RadioRepository
import com.toyent.hiqradio.databinding.FragmentFavoritesBinding
import com.toyent.hiqradio.ui.activity.MainActivity
import com.toyent.hiqradio.ui.adapter.FavoriteAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragment displaying favorite stations
 */
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RadioRepository
    private lateinit var adapter: FavoriteAdapter

    private val favorites = mutableListOf<FavoriteStation>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = RadioRepository.getInstance(requireContext())

        setupRecyclerView()
        observeFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = FavoriteAdapter(
            favorites = favorites,
            onItemClick = { favorite ->
                val station = Station(
                    stationUuid = favorite.stationUuid,
                    name = favorite.name,
                    urlResolved = favorite.urlResolved,
                    homepage = favorite.homepage,
                    favicon = favorite.favicon,
                    tags = favorite.tags,
                    country = favorite.country,
                    countryCode = favorite.countryCode,
                    state = favorite.state,
                    language = favorite.language,
                    codec = favorite.codec,
                    bitrate = favorite.bitrate,
                    isCustom = 1
                )
                (activity as? MainActivity)?.playStation(station)
            },
            onRemoveClick = { favorite ->
                removeFromFavorites(favorite)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@FavoritesFragment.adapter
        }
    }

    private fun observeFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllFavorites().collectLatest { favoritesList ->
                favorites.clear()
                favorites.addAll(favoritesList)
                adapter.notifyDataSetChanged()
                updateEmptyState()
            }
        }
    }

    private fun removeFromFavorites(favorite: FavoriteStation) {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.removeFromFavorites(favorite.stationUuid)
        }
    }

    private fun updateEmptyState() {
        binding.emptyView.visibility = if (favorites.isEmpty()) View.VISIBLE else View.GONE
    }
}
