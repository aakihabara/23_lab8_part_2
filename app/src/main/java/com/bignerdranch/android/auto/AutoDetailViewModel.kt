package com.bignerdranch.android.auto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class AutoDetailViewModel() : ViewModel() {
    private val autoRepository = AutoRepository.get()
    private val autoIdLiveData = MutableLiveData<UUID>()
    var autoLiveData: LiveData<Auto?> =
        Transformations.switchMap(autoIdLiveData) { autoId ->
            autoRepository.getAuto(autoId)
        }
    fun loadAuto(autoId: UUID) {
        autoIdLiveData.value = autoId
    }

    fun saveAuto(auto: Auto) {
        autoRepository.updateAuto(auto)
    }
}