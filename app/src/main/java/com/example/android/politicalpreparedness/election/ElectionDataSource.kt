package com.example.android.politicalpreparedness.election.data

import com.example.android.politicalpreparedness.utils.Result
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionResponse
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse

interface ElectionDataSource {
    suspend fun electionFromApi(): Result<ElectionResponse>
    suspend fun savedElection(): Result<List<Election>>
    suspend fun getVoterInfo(map: Map<String, Any>) : Result<VoterInfoResponse>
    suspend fun saveElection(election: Election)
    suspend fun deleteElection(electionId: Int)
}