package com.example.android.politicalpreparedness.election

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.example.android.politicalpreparedness.repository.ElectionRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception

class VoterInfoViewModel @ViewModelInject constructor(
        private val electionRepository: ElectionRepository,
        @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {

    // Add live data to hold voter info
    private val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse> get() = _voterInfo

    private val _intentUrl = MutableLiveData<String>()
    val intentUrl: LiveData<String> get() = _intentUrl

    // Add var and methods to support loading URLs
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // cont'd -- Populate initial state of save button to reflect proper action based on election saved status
    val isFollow : LiveData<Boolean> = electionRepository.isElectionExist(savedStateHandle.get<Int>("arg_election_id")!!).asLiveData()

    private val electionId: Int = savedStateHandle.get<Int>("arg_election_id")!!

    init {
        Timber.i("init ViewModel()")
        loadVoterInfo()
    }

    // Add var and methods to populate voter info
    private fun loadVoterInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            val division: Division = savedStateHandle.get<Division>("arg_division")!!

            try {
                val response = if (division.state == "ok")
                    electionRepository.getVoterInfo("oklahoma", electionId)
                else
                    electionRepository.getVoterInfo(division.state, electionId)

                if (response.isSuccessful)
                    _voterInfo.value = response.body()
                else {
                    _voterInfo.value = null
                    Timber.i("response error: ${response.errorBody()?.charStream()?.readText()}")
                }
            }
            catch (ex: Exception) { Timber.e(ex) }
            finally { _isLoading.value = false }
        }
    }

    fun setLocationIntent() {
        _voterInfo.value?.let {
            if (!it.state.isNullOrEmpty())
                _intentUrl.value = it.state.first().electionAdministrationBody.votingLocationFinderUrl
        }
    }

    fun setBallotIntent() {
        _voterInfo.value?.let {
            if (!it.state.isNullOrEmpty())
                _intentUrl.value = it.state.first().electionAdministrationBody.ballotInfoUrl
        }
    }

    // Add var and methods to save and remove elections to local database
    fun setFollowOrUnfollow() {
        isFollow.value?.let { isFollow ->
            if (isFollow)
                viewModelScope.launch { electionRepository.deleteSavedElection(electionId) }
            else
                viewModelScope.launch { electionRepository.insertSavedElection(_voterInfo.value!!.election) }
        }
    }
}