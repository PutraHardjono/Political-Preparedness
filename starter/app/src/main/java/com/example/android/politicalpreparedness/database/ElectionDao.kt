package com.example.android.politicalpreparedness.database

import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.flow.Flow

@Dao
interface ElectionDao {

    // Add insert query
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(election: Election): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(listElection: List<Election>)

    // Add select all election query
    @Query("SELECT * FROM election_table ORDER BY name ASC")
    fun getElections(): Flow<List<Election>>

    // Add select single election query
    @Query("SELECT * FROM election_table WHERE id = :electionId")
    fun getElectionById(electionId: Int): Flow<Election?>

    @Query("SELECT EXISTS(SELECT 1 FROM election_table WHERE id = :electionId LIMIT 1)")
    fun checkElectionById(electionId: Int): Flow<Boolean>

    // Add delete query
    @Query("DELETE FROM election_table WHERE id=:electionId")
    suspend fun deleteElectionById(electionId: Int)

    // Add clear query
    @Query("DELETE FROM election_table")
    fun clearElection()
}