package com.example.android.politicalpreparedness.election.data

import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.utils.Result
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionResponse
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class ElectionRepository(
        private val retrofitService: CivicsApiService,
        private val electionDao: ElectionDao,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ElectionDataSource {

    override suspend fun electionFromApi(): Result<ElectionResponse> {
        return withContext(ioDispatcher) {
            return@withContext try {
                retrofitService.getElections()?.let {
                    return@let Result.Success(retrofitService.getElections())
                } ?: kotlin.run {
                    return@run Result.Error(R.string.error_message_data_is_null.toString())
                }
            } catch (e: Exception) {
                Result.Error("${e.message}\n${e.cause?.message.orEmpty()}")
            }
        }
    }

    override suspend fun savedElection(): Result<List<Election>> {
        return withContext(ioDispatcher) {
            return@withContext try {
                Result.Success(electionDao.getElections())
            } catch (e: Exception) {
                Result.Error(e.message ?: R.string.error_message_get_saved_election_method.toString())
            }
        }
    }

    override suspend fun saveElection(election: Election) {
        withContext(ioDispatcher) {
            electionDao.saveElection(election)
        }
    }

    override suspend fun deleteElection(electionId: Int) {
        withContext(ioDispatcher) {
            electionDao.deleteElectionById(electionId)
        }
    }

    override suspend fun getVoterInfo(map: Map<String, Any>): Result<VoterInfoResponse> {
        return withContext(ioDispatcher) {
            return@withContext try {
                Result.Success(retrofitService.getVoterInfo(map))
            } catch (e: Exception) {
                Result.Error("${e.message}\n${e.cause?.message.orEmpty()}")
            }
        }
    }

}