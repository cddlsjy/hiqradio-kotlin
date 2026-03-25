package com.toyent.hiqradio.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.toyent.hiqradio.data.model.RecentlyPlayed
import com.toyent.hiqradio.data.model.Station
import com.toyent.hiqradio.data.repository.RadioRepository
import com.toyent.hiqradio.databinding.FragmentRecentlyPlayedBinding
import com.toyent.hiqradio.ui.activity.MainActivity
import com.toyent.hiqradio.ui.adapter.RecentlyPlayedAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragment displaying recently played stations
 */
class RecentlyPlayedFragment : Fragment() {

    private var _binding: FragmentRecentlyPlayedBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RadioRepository
    private lateinit var adapter: RecentlyPlayedAdapter

    private val recentlyPlayed = mutableListOf<RecentlyPlayed>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecentlyPlayedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = RadioRepository.getInstance(requireContext())

        setupRecyclerView()
        observeRecentlyPlayed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = RecentlyPlayedAdapter(
            recentlyPlayed = recentlyPlayed,
            onItemClick = { recently ->
                val station = Station(
                    stationUuid = recently.stationUuid,
                    name = recently.name,
                    urlResolved = recently.urlResolved,
                    homepage = recently.homepage,
                    favicon = recently.favicon,
                    tags = recently.tags,
                    country = recently.country,
                    countryCode = recently.countryCode,
                    state = recently.state,
                    language = recently.language,
                    codec = recently.codec,
                    bitrate = recently.bitrate
                )
                (activity as? MainActivity)?.playStation(station)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@RecentlyPlayedFragment.adapter
        }
    }

    private fun observeRecentlyPlayed() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getRecentlyPlayed(50).collectLatest { list ->
                recentlyPlayed.clear()
                recentlyPlayed.addAll(list)
                adapter.notifyDataSetChanged()
                updateEmptyState()
            }
        }
    }

    private fun updateEmptyState() {
        binding.emptyView.visibility = if (recentlyPlayed.isEmpty()) View.VISIBLE else View.GONE
    }
}
