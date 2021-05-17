package com.example.android.politicalpreparedness.representative

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.getRepresentativeDeferred
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel : ViewModel() {

    val mutableAddressLiveData = MutableLiveData<Address>().apply {
        value = Address()
    }
    private val addressInput: LiveData<Address>
        get() = mutableAddressLiveData

    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>?>
        get() = _representatives


    fun onFindRepresentativeClick() {
        fetchRepresentatives()
    }

    private fun fetchRepresentatives() {
        viewModelScope.launch {
            try {
                val (offices, officials) = getRepresentativeDeferred(mapOf("address" to addressInput.value?.toFormattedString())).await()
                _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }
            } catch (e: Exception) {
                Log.e("Error fetch Data", "Error 1 ${e.message}")
            }
        }
    }

    fun getGeolocationAddress(address: Address) {
        mutableAddressLiveData.value = address
        fetchRepresentatives()
    }
}
