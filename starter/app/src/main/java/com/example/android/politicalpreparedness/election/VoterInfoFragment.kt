package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.util.fadeIn
import com.example.android.politicalpreparedness.util.fadeOut
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class VoterInfoFragment : Fragment() {

    private lateinit var binding: FragmentVoterInfoBinding

    // Add ViewModel values and create ViewModel
    private val _vm by viewModels<VoterInfoViewModel>()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Add binding values
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_voter_info, container, false)

        // Populate voter info -- hide views without provided data.
        _vm.voterInfo.observe(viewLifecycleOwner) { voteInfo ->
            voteInfo?.let {
                Timber.i("electionName: ${it.election.name}, state: ${it.state}")
            }
        }

        // Handle loading of URLs
        _vm.intentUrl.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                startActivity(it)
            }
        }

        // Handle save button UI state
        _vm.isFollow.observe(viewLifecycleOwner) { isFollow ->
            isFollow?.let {
                if (isFollow)
                    binding.buttonFollow.text = getString(R.string.unfollow_election)
                else
                    binding.buttonFollow.text = getString(R.string.follow_election)
            }
        }

        // Handle upcomingElectionProgressBar
        _vm.isLoading.observe(viewLifecycleOwner) { isLoading ->
            isLoading?.let {
                if (isLoading)
                    binding.upcomingElectionProgressBar.fadeIn()
                else {
                    binding.upcomingElectionProgressBar.fadeOut()
                }
            }
        }

        binding.viewModel = _vm
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    // Create method to load URL intents
    private fun startActivity(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    // cont'd Handle save button clicks = DONE in Fragment_voter_info.xml
}