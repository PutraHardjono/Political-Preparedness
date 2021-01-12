package com.example.android.politicalpreparedness.election

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.repository.ElectionRepository
import kotlinx.coroutines.launch
import timber.log.Timber

// Construct ViewModel and provide election datasource
class ElectionsViewModel @ViewModelInject constructor(
        private val electionRepository: ElectionRepository,
        @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {

    // Create live data val for upcoming elections
    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>> get() = _upcomingElections

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Create live data val for saved elections
    val savedElections = electionRepository.getSavedElections().asLiveData()

    init {
        _isLoading.value = true
        reloadUpcomingElection()
    }

    // Create val and functions to populate live data for upcoming elections from the API and saved elections from local database
    fun reloadUpcomingElection() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = electionRepository.getElections()
                if (response.isSuccessful)
                    _upcomingElections.value = response.body()!!.elections
            }
            catch (ex: Exception) {
                Timber.e(ex)
            }
            finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _upcomingElections.value = null
        _isLoading.value = null
    }

    // Create functions to navigate to saved or upcoming election voter info
}