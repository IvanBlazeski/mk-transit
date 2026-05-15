package mk.fikt.mktransit.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mk.fikt.mktransit.domain.model.Stop
import javax.inject.Inject

data class MapStop(
    val stopName: String,
    val latitude: Double,
    val longitude: Double,
    val lineName: String,
    val lineNumber: String
)

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation

    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted

    private val _mapStops = MutableStateFlow<List<MapStop>>(emptyList())
    val mapStops: StateFlow<List<MapStop>> = _mapStops

    init {
        loadStopsFromFirestore()
    }

    fun setPermissionGranted(granted: Boolean) {
        _locationPermissionGranted.value = granted
    }

    @SuppressLint("MissingPermission")
    fun fetchUserLocation(context: Context) {
        viewModelScope.launch {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY, null
                ).await()
                _userLocation.value = location
            } catch (e: Exception) { }
        }
    }

    fun loadStopsFromFirestore() {
        viewModelScope.launch {
            try {
                val linesSnapshot = firestore.collection("lines").get().await()
                val allStops = mutableListOf<MapStop>()

                linesSnapshot.documents.forEach { lineDoc ->
                    val lineName = lineDoc.getString("lineName") ?: ""
                    val lineNumber = lineDoc.getString("lineNumber") ?: ""

                    val stopsSnapshot = firestore.collection("lines")
                        .document(lineDoc.id)
                        .collection("stops")
                        .get().await()

                    stopsSnapshot.documents.forEach { stopDoc ->
                        val lat = stopDoc.getDouble("latitude") ?: 0.0
                        val lon = stopDoc.getDouble("longitude") ?: 0.0
                        val stopName = stopDoc.getString("stopName") ?: ""

                        // Само стопови со вистински координати
                        if (lat != 0.0 && lon != 0.0) {
                            allStops.add(MapStop(
                                stopName = stopName,
                                latitude = lat,
                                longitude = lon,
                                lineName = lineName,
                                lineNumber = lineNumber
                            ))
                        }
                    }
                }
                _mapStops.value = allStops
            } catch (e: Exception) { }
        }
    }
}