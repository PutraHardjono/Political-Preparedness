package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import com.example.android.politicalpreparedness.util.Connectivity.isOnline
import com.example.android.politicalpreparedness.util.fadeIn
import com.example.android.politicalpreparedness.util.fadeOut
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ElectionsFragment: Fragment() {

    // Declare ViewModel
    // Add ViewModel values and create ViewModel
    private val _vm by viewModels<ElectionsViewModel>()
    private lateinit var binding: FragmentElectionBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        //Add binding values
        binding = FragmentElectionBinding.inflate(inflater)

        _vm.isLoading.observe(viewLifecycleOwner) { isLoading ->
            isLoading?.let {
                if (isLoading)
                    binding.upcomingElectionProgressBar.fadeIn()
                else {
                    binding.upcomingElectionProgressBar.fadeOut()
                    if (binding.refreshLayout.isRefreshing)
                        binding.refreshLayout.isRefreshing = false
                }
            }
        }

        // Populate recycler adapters
        _vm.upcomingElections.observe(viewLifecycleOwner) {
            (binding.upcomingRecyclerView.adapter as ElectionListAdapter).submitList(it)
        }
        _vm.savedElections.observe(viewLifecycleOwner) {
            (binding.savedElectionRecyclerView.adapter as ElectionListAdapter).submitList(it)
        }

        // Refresh adapters when fragment loads
        binding.refreshLayout.setOnRefreshListener { _vm.reloadUpcomingElection() }

        if(!isOnline(requireContext()))
            Toast.makeText(requireContext(),"No Internet Connection", Toast.LENGTH_LONG).show()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
    }

    // Initiate recycler adapters
    private fun setRecyclerView() {
        val upcomingElectionAdapter = ElectionListAdapter(ElectionListener {
            // Link elections to voter info
            if (it.division.state.isNotEmpty())
                findNavController().navigate(ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(it.id, it.division))
            else
                Toast.makeText(requireContext(), getString(R.string.no_vote_info), Toast.LENGTH_LONG).show()
        })
        binding.upcomingRecyclerView.adapter = upcomingElectionAdapter

        val savedElectionAdapter = ElectionListAdapter(ElectionListener {
            findNavController().navigate(ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(it.id, it.division))
        })
        binding.savedElectionRecyclerView.adapter = savedElectionAdapter
    }
}