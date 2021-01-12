package com.example.android.politicalpreparedness.repository

import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ElectionRepository @Inject constructor(
        private val service: CivicsApiService,
        private val electionDao: ElectionDao
) {

    suspend fun refreshElections() {
        withContext(Dispatchers.IO) {
            try {
                val response = service.getElections()
                if (response.isSuccessful) {
                    val list = response.body()!!.elections
                    Timber.i(">>> LIST: $list")
                    electionDao.insertAll(list)
                }
            } catch (ex: Exception) {
                Timber.e(ex.localizedMessage)
            }
        }
    }

    // Get upcoming election from api
    suspend fun getElections() = service.getElections()

    // Get voter info from api
    suspend fun getVoterInfo(address: String, electionId: Int) = service.getVoterInfo(address, electionId)

    // Get representatives from api
    suspend fun getRepresentative(address: String) = service.getRepresentatives(address)

    // Get saved election from database
    fun getSavedElections() = electionDao.getElections()



    // Check whether election is exist in database
    fun isElectionExist(electionId: Int) = electionDao.checkElectionById(electionId)

    suspend fun deleteSavedElection(electionId: Int) {
        withContext(Dispatchers.IO) {
            electionDao.deleteElectionById(electionId)
        }
    }

    suspend fun insertSavedElection(election: Election) {
        withContext(Dispatchers.IO) {
            electionDao.insert(election)
        }
    }

}