package mk.fikt.mktransit.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation

    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted

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
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}