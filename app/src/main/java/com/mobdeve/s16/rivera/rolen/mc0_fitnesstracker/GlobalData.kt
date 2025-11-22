package com.mobdeve.s16.rivera.rolen.mc0_fitnesstracker

import androidx.lifecycle.MutableLiveData
import org.osmdroid.util.GeoPoint

// This object acts like a "Shared Notepad" accessible from anywhere in the app
object GlobalData {
    // LiveData lets the screen know automatically when these numbers change
    val liveDistance = MutableLiveData(0f) // Distance in meters
    val liveSteps = MutableLiveData(0)     // Step count
    val liveCalories = MutableLiveData(0)  // Burned calories

    // A list of GPS points to draw the line on the map
    val liveRoute = MutableLiveData<MutableList<GeoPoint>>(mutableListOf())

    // A simple flag to know if we are currently running
    var isTracking = false

    // Call this when hitting "Stop" to clear the data for next time
    fun clearData() {
        liveDistance.postValue(0f)
        liveSteps.postValue(0)
        liveCalories.postValue(0)
        liveRoute.postValue(mutableListOf())
    }
}