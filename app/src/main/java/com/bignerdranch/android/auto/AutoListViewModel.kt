package com.bignerdranch.android.auto

import androidx.lifecycle.ViewModel
import kotlin.random.Random

class AutoListViewModel : ViewModel() {

    private val autoRepository = AutoRepository.get()
    val autoListLiveData = autoRepository.getAutos()

    fun addAuto(auto: Auto) {
        autoRepository.addAuto(auto)
    }

}