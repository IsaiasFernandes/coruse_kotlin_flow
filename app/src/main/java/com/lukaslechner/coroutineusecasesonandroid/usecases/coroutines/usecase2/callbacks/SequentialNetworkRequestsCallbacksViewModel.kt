package com.lukaslechner.coroutineusecasesonandroid.usecases.coroutines.usecase2.callbacks

import com.lukaslechner.coroutineusecasesonandroid.base.BaseViewModel
import com.lukaslechner.coroutineusecasesonandroid.mock.AndroidVersion
import com.lukaslechner.coroutineusecasesonandroid.mock.VersionFeatures
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SequentialNetworkRequestsCallbacksViewModel(
    private val mockApi: CallbackMockApi = mockApi()
) : BaseViewModel<UiState>() {

    private var getAndroidVersionsCall: Call<List<AndroidVersion>>? = null
    private var getAndroidFeaturesCall: Call<VersionFeatures>? = null

    fun perform2SequentialNetworkRequest() {
        uiState.value = UiState.Loading

        getAndroidVersionsCall = mockApi.getRecentAndroidVersions()
        getAndroidVersionsCall!!.enqueue(object: Callback<List<AndroidVersion>>{
            override fun onResponse(
                call: Call<List<AndroidVersion>>,
                response: Response<List<AndroidVersion>>
            ) {
                if(response.isSuccessful) {
                    val mostRecentVersion = response.body()?.last()
                    val level  = if(mostRecentVersion?.apiLevel != null ) { mostRecentVersion.apiLevel } else { 0 }
                    getAndroidFeaturesCall = mockApi.getAndroidVersionFeatures(level)
                    getAndroidFeaturesCallback(getAndroidFeaturesCall!!)
                } else {
                    uiState.value = UiState.Error("Network request failed!")
                }
            }

            override fun onFailure(p0: Call<List<AndroidVersion>>, p1: Throwable) {
                uiState.value = UiState.Error("Something unexpected happened!")
            }

        })
    }

    private fun getAndroidFeaturesCallback(getAndroidFeaturesCall: Call<VersionFeatures>) {
        getAndroidFeaturesCall.enqueue(object : Callback<VersionFeatures> {
            override fun onResponse(
                call: Call<VersionFeatures>,
                response: Response<VersionFeatures>
            ) {
                if(response.isSuccessful) {
                    val featuresOfMostRecentVersion = response.body()
                    featuresOfMostRecentVersion?.let {
                        uiState.value = UiState.Success(it)
                    }
                } else {
                    uiState.value = UiState.Error("Network request failed!")
                }
            }

            override fun onFailure(p0: Call<VersionFeatures>, p1: Throwable) {
                uiState.value = UiState.Error("Something unexpected happened!")
            }
        })
    }

    override fun onCleared() {
        super.onCleared()

        getAndroidVersionsCall?.cancel()
        getAndroidFeaturesCall?.cancel()
    }
}