package com.example.android.politicalpreparedness.network

import com.example.android.politicalpreparedness.network.jsonadapter.ElectionAdapter
import com.example.android.politicalpreparedness.network.models.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.QueryMap
import java.util.*

private const val BASE_URL = "https://www.googleapis.com/civicinfo/v2/"

private val moshi = Moshi.Builder()
        .add(Date::class.java, Rfc3339DateJsonAdapter())
        .add(ElectionAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(CivicsHttpClient.getClient())
        .baseUrl(BASE_URL)
        .build()

/**
 *  Documentation for the Google Civics API Service can be found at https://developers.google.com/civic-information/docs/v2
 */

@JvmSuppressWildcards
interface CivicsApiService {

    @GET("elections")
    suspend fun getElections(): ElectionResponse?

    @GET("voterinfo")
    suspend fun getVoterInfo(@QueryMap map: Map<String, Any>): VoterInfoResponse?

    @GET("representatives")
    suspend fun getRepresentativeDeferredAsync(@QueryMap map: Map<String, Any?>): RepresentativeResponse
}

object CivicsApi {
    val retrofitService: CivicsApiService by lazy {
        retrofit.create(CivicsApiService::class.java)
    }
}

suspend fun getRepresentativeDeferred(map: Map<String, Any?>): Deferred<Pair<List<Office>, List<Official>>> {
    return coroutineScope {
        return@coroutineScope async {
            val representativeResponse = CivicsApi.retrofitService.getRepresentativeDeferredAsync(map)
            return@async Pair(representativeResponse.offices,representativeResponse.officials)
        }
    }
}