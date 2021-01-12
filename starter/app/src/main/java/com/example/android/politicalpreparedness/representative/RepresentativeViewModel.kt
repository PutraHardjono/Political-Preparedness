package com.example.android.politicalpreparedness.representative

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.repository.ElectionRepository
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber

/**
 *  The following code will prove helpful in constructing a representative from the API. This code combines the two nodes of the RepresentativeResponse into a single official :

val (offices, officials) = getRepresentativesDeferred.await()
_representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }

Note: getRepresentatives in the above code represents the method used to fetch data from the API
Note: _representatives in the above code represents the established mutable live data housing representatives

 */

class RepresentativeViewModel @ViewModelInject constructor(
        private val repository: ElectionRepository,
        @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    //Establish live data for representatives and address
    private val _representative = MutableLiveData<List<Representative>>()
    val representative: LiveData<List<Representative>> get() = _representative

    private val _address = MutableLiveData<Address>()
    val address : LiveData<Address> get() = _address

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> get() = _errorMessage

    // Create function to fetch representatives from API from a provided address
    private fun fetchRepresentative(addressFormattedString: String) {
        viewModelScope.launch {
            try {
                val response = repository.getRepresentative(addressFormattedString)
                if (response.isSuccessful) {
                    val (offices, officials) = response.body()!!
                    _representative.value = offices.flatMap { office -> office.getRepresentatives(officials) }
                }
                else {
                    _representative.value = null
                    val message = JSONObject(response.errorBody()!!.charStream().readText())
                            .getJSONObject("error")
                            .getString("message")
                    Timber.e("response.errorBody: $message")
                    _errorMessage.value = message
                }
            }
            catch (ex: Exception) { Timber.e(ex) }
        }
    }

    // Create function get address from geo location
    fun setAddress(address: Address) {
        _address.value = address
        fetchRepresentative(address.toFormattedString())
    }

    // Create function to get address from individual fields
    fun setAddress(line1: String, line2: String, city: String, state: String, zip: String ) {
        val address = Address(line1, line2, city,state,zip)
        fetchRepresentative(address.toFormattedString())
    }

    override fun onCleared() {
        super.onCleared()
        _representative.value = null
        _address.value = null
    }
}
