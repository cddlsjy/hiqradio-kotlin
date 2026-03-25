package com.toyent.hiqradio.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.toyent.hiqradio.R
import com.toyent.hiqradio.data.model.Station
import com.toyent.hiqradio.data.repository.RadioRepository
import com.toyent.hiqradio.databinding.FragmentStationsBinding
import com.toyent.hiqradio.ui.activity.MainActivity
import com.toyent.hiqradio.ui.adapter.StationAdapter
import kotlinx.coroutines.launch

/**
 * Fragment displaying top voted stations
 */
class StationsFragment : Fragment() {

    private var _binding: FragmentStationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RadioRepository
    private lateinit var adapter: StationAdapter

    private var stations = mutableListOf<Station>()
    private var isLoading = false
    private var currentQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = RadioRepository.getInstance(requireContext())

        setupRecyclerView()
        setupSearch()
        setupSwipeRefresh()

        loadTopStations()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = StationAdapter(
            stations = stations,
            onItemClick = { station ->
                (activity as? MainActivity)?.playStation(station)
            },
            onFavoriteClick = { station ->
                (activity as? MainActivity)?.toggleFavorite(station)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@StationsFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotEmpty()) {
                        currentQuery = it
                        searchStations(it)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            if (currentQuery.isNotEmpty()) {
                searchStations(currentQuery)
            } else {
                loadTopStations()
            }
        }
    }

    private fun loadTopStations() {
        if (isLoading) return

        isLoading = true
        binding.swipeRefresh.isRefreshing = true
        binding.emptyView.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            repository.getTopStations(100).fold(
                onSuccess = { stationsList ->
                    stations.clear()
                    stations.addAll(stationsList)
                    adapter.notifyDataSetChanged()
                    updateEmptyState()
                },
                onFailure = { error ->
                    Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                }
            )

            isLoading = false
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun searchStations(query: String) {
        if (isLoading) return

        isLoading = true
        binding.swipeRefresh.isRefreshing = true
        binding.emptyView.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            repository.searchStations(name = query).fold(
                onSuccess = { stationsList ->
                    stations.clear()
                    stations.addAll(stationsList)
                    adapter.notifyDataSetChanged()
                    updateEmptyState()
                },
                onFailure = { error ->
                    Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                }
            )

            isLoading = false
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun updateEmptyState() {
        binding.emptyView.visibility = if (stations.isEmpty()) View.VISIBLE else View.GONE
    }
}
