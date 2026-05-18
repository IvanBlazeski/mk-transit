package mk.fikt.mktransit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mk.fikt.mktransit.domain.model.BusLine
import mk.fikt.mktransit.domain.model.LineType
import mk.fikt.mktransit.domain.model.OperatorProfile
import javax.inject.Inject
import android.content.Context

sealed class OperatorState {
    object Loading : OperatorState()
    object Idle : OperatorState()
    data class ProfileLoaded(val profile: OperatorProfile) : OperatorState()
    data class LinesLoaded(val lines: List<BusLine>) : OperatorState()
    data class Error(val message: String) : OperatorState()
    object SaveSuccess : OperatorState()
}

@HiltViewModel
class OperatorViewModel @Inject constructor() : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<OperatorState>(OperatorState.Idle)
    val state: StateFlow<OperatorState> = _state

    private val _profile = MutableStateFlow<OperatorProfile?>(null)
    val profile: StateFlow<OperatorProfile?> = _profile

    private val _lines = MutableStateFlow<List<BusLine>>(emptyList())
    val lines: StateFlow<List<BusLine>> = _lines

    // Вчитај профил на операторот
    fun loadOperatorProfile() {
        viewModelScope.launch {
            _state.value = OperatorState.Loading
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val snapshot = firestore.collection("operators")
                    .whereEqualTo("uid", uid)
                    .get().await()

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val profile = OperatorProfile(
                        operatorId = doc.id,
                        uid = uid,
                        companyName = doc.getString("companyName") ?: "",
                        logoUrl = doc.getString("logoUrl") ?: "",
                        description = doc.getString("description") ?: "",
                        contactEmail = doc.getString("contactEmail") ?: "",
                        contactPhone = doc.getString("contactPhone") ?: "",
                        coverageArea = doc.getString("coverageArea") ?: ""
                    )
                    _profile.value = profile
                    _state.value = OperatorState.ProfileLoaded(profile)
                    loadOperatorLines(doc.id)
                } else {
                    _state.value = OperatorState.Idle
                }
            } catch (e: Exception) {
                _state.value = OperatorState.Error(e.message ?: "Failed")
            }
        }
    }

    fun addStopWithGeocoding(context: Context, lineId: String, stopName: String, locationText: String, minutesFromStart: Int) {
        viewModelScope.launch {
            try {
                var lat = 0.0
                var lon = 0.0

                // Nominatim API — OpenStreetMap geocoding
                try {
                    val query = java.net.URLEncoder.encode(locationText, "UTF-8")
                    val url = "https://nominatim.openstreetmap.org/search?q=$query&format=json&limit=1&countrycodes=mk"
                    android.util.Log.d("Geocoding", "URL: $url")

                    val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        java.net.URL(url).openConnection().apply {
                            setRequestProperty("User-Agent", "MKTransit/1.0")
                            setRequestProperty("Accept", "application/json")
                            connectTimeout = 10000
                            readTimeout = 10000
                        }.getInputStream().bufferedReader().readText()
                    }

                    android.util.Log.d("Geocoding", "Response: $response")

                    val jsonArray = org.json.JSONArray(response)
                    if (jsonArray.length() > 0) {
                        val first = jsonArray.getJSONObject(0)
                        lat = first.getDouble("lat")
                        lon = first.getDouble("lon")
                        android.util.Log.d("Geocoding", "Found: $lat, $lon")
                    } else {
                        android.util.Log.d("Geocoding", "No results!")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("Geocoding", "Error: ${e.message}")
                }

                val order = _stops.value.size
                val stop = hashMapOf(
                    "stopName" to stopName,
                    "stopOrder" to order,
                    "minutesFromStart" to minutesFromStart,
                    "latitude" to lat,
                    "longitude" to lon,
                    "locationText" to locationText
                )
                firestore.collection("lines")
                    .document(lineId)
                    .collection("stops")
                    .add(stop).await()
                _state.value = OperatorState.SaveSuccess
                loadStops(lineId)
            } catch (e: Exception) {
                _state.value = OperatorState.Error(e.message ?: "Failed to add stop")
            }
        }
    }

    // Зачувај профил
    fun saveOperatorProfile(
        companyName: String,
        description: String,
        contactEmail: String,
        contactPhone: String,
        coverageArea: String
    ) {
        viewModelScope.launch {
            _state.value = OperatorState.Loading
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val data = hashMapOf(
                    "uid" to uid,
                    "companyName" to companyName,
                    "description" to description,
                    "contactEmail" to contactEmail,
                    "contactPhone" to contactPhone,
                    "coverageArea" to coverageArea,
                    "logoUrl" to ""
                )

                val existing = firestore.collection("operators")
                    .whereEqualTo("uid", uid).get().await()

                if (existing.isEmpty) {
                    firestore.collection("operators").add(data).await()
                } else {
                    firestore.collection("operators")
                        .document(existing.documents[0].id)
                        .set(data).await()
                }
                _state.value = OperatorState.SaveSuccess
                loadOperatorProfile()
            } catch (e: Exception) {
                _state.value = OperatorState.Error(e.message ?: "Failed to save")
            }
        }
    }

    // Вчитај линии на операторот
    fun loadOperatorLines(operatorId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("lines")
                    .get().await()

                val lines = snapshot.documents.mapNotNull { doc ->
                    try {
                        BusLine(
                            lineId = doc.id,
                            operatorId = doc.getString("operatorId") ?: "",
                            lineNumber = doc.getString("lineNumber") ?: "",
                            lineName = doc.getString("lineName") ?: "",
                            lineType = try {
                                LineType.valueOf(doc.getString("lineType") ?: "BUS")
                            } catch (e: Exception) { LineType.BUS },
                            startStop = doc.getString("startStop") ?: "",
                            endStop = doc.getString("endStop") ?: "",
                            isActive = doc.getBoolean("isActive") ?: true,
                            averageRating = doc.getDouble("averageRating")?.toFloat() ?: 0f,
                            ratingCount = doc.getLong("ratingCount")?.toInt() ?: 0,
                            priceOneWay = doc.getDouble("priceOneWay")?.toFloat() ?: 50f,
                            priceReturn = doc.getDouble("priceReturn")?.toFloat() ?: 90f
                        )
                    } catch (e: Exception) { null }
                }
                _lines.value = lines
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    // Создај нова линија
    fun createLine(
        lineNumber: String,
        lineName: String,
        lineType: LineType,
        startStop: String,
        endStop: String,
        priceOneWay: Float = 50f,
        priceReturn: Float = 90f
    ) {
        viewModelScope.launch {
            _state.value = OperatorState.Loading
            try {
                val operatorId = _profile.value?.operatorId ?: return@launch
                val line = hashMapOf(
                    "operatorId" to operatorId,
                    "lineNumber" to lineNumber,
                    "lineName" to lineName,
                    "lineType" to lineType.name,
                    "startStop" to startStop,
                    "endStop" to endStop,
                    "isActive" to true,
                    "averageRating" to 0.0,
                    "ratingCount" to 0,
                    "priceOneWay" to priceOneWay,
                    "priceReturn" to priceReturn
                )
                firestore.collection("lines").add(line).await()
                _state.value = OperatorState.SaveSuccess
                loadOperatorLines(operatorId)
            } catch (e: Exception) {
                _state.value = OperatorState.Error(e.message ?: "Failed to create line")
            }
        }
    }

    fun updateLinePrices(lineId: String, priceOneWay: Float, priceReturn: Float) {
        viewModelScope.launch {
            try {
                firestore.collection("lines").document(lineId)
                    .update("priceOneWay", priceOneWay, "priceReturn", priceReturn).await()
                val operatorId = _profile.value?.operatorId ?: return@launch
                loadOperatorLines(operatorId)
            } catch (e: Exception) { }
        }
    }

    // Избриши линија
    fun deleteLine(lineId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("lines").document(lineId).delete().await()
                val operatorId = _profile.value?.operatorId ?: return@launch
                loadOperatorLines(operatorId)
            } catch (e: Exception) {
                _state.value = OperatorState.Error(e.message ?: "Failed to delete")
            }
        }
    }

    // Вчитај стопови на линија
    private val _stops = MutableStateFlow<List<mk.fikt.mktransit.domain.model.Stop>>(emptyList())
    val stops: StateFlow<List<mk.fikt.mktransit.domain.model.Stop>> = _stops

    fun loadStops(lineId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("lines")
                    .document(lineId)
                    .collection("stops")
                    .orderBy("stopOrder")
                    .get().await()

                val stops = snapshot.documents.mapNotNull { doc ->
                    try {
                        mk.fikt.mktransit.domain.model.Stop(
                            stopId = doc.id,
                            stopName = doc.getString("stopName") ?: "",
                            stopOrder = doc.getLong("order")?.toInt() ?: 0,
                            minutesFromStart = doc.getLong("minutesFromStart")?.toInt() ?: 0,
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0
                        )
                    } catch (e: Exception) { null }
                }
                _stops.value = stops
            } catch (e: Exception) { }
        }
    }

    fun addStop(lineId: String, stopName: String, minutesFromStart: Int) {
        viewModelScope.launch {
            try {
                val order = _stops.value.size
                val stop = hashMapOf(
                    "stopName" to stopName,
                    "stopOrder" to order,
                    "minutesFromStart" to minutesFromStart,
                    "latitude" to 0.0,
                    "longitude" to 0.0
                )
                firestore.collection("lines")
                    .document(lineId)
                    .collection("stops")
                    .add(stop).await()
                _state.value = OperatorState.SaveSuccess
                loadStops(lineId)
            } catch (e: Exception) {
                _state.value = OperatorState.Error(e.message ?: "Failed to add stop")
            }
        }
    }

    fun deleteStop(lineId: String, stopId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("lines")
                    .document(lineId)
                    .collection("stops")
                    .document(stopId)
                    .delete().await()
                loadStops(lineId)
            } catch (e: Exception) { }
        }
    }

    private val _schedule = MutableStateFlow<List<mk.fikt.mktransit.domain.model.Schedule>>(emptyList())
    val schedule: StateFlow<List<mk.fikt.mktransit.domain.model.Schedule>> = _schedule

    fun loadSchedule(lineId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("lines")
                    .document(lineId)
                    .collection("schedule")
                    .orderBy("direction")
                    .get().await()

                val schedules = snapshot.documents.mapNotNull { doc ->
                    try {
                        mk.fikt.mktransit.domain.model.Schedule(
                            scheduleId = doc.id,
                            direction = doc.getString("direction") ?: "FORWARD",
                            departureTime = doc.getString("departureTime") ?: "",
                            days = (doc.get("days") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                        )
                    } catch (e: Exception) { null }
                }
                _schedule.value = schedules
            } catch (e: Exception) { }
        }
    }

    fun addSchedule(lineId: String, direction: String, departureTime: String, days: List<String>) {
        viewModelScope.launch {
            try {
                val entry = hashMapOf(
                    "direction" to direction,
                    "departureTime" to departureTime,
                    "days" to days
                )
                firestore.collection("lines")
                    .document(lineId)
                    .collection("schedule")
                    .add(entry).await()
                loadSchedule(lineId)
            } catch (e: Exception) { }
        }
    }

    fun deleteSchedule(lineId: String, scheduleId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("lines")
                    .document(lineId)
                    .collection("schedule")
                    .document(scheduleId)
                    .delete().await()
                loadSchedule(lineId)
            } catch (e: Exception) { }
        }
    }

    fun loadAllLines() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("lines").get().await()
                val lines = snapshot.documents.mapNotNull { doc ->
                    try {
                        BusLine(
                            lineId = doc.id,
                            operatorId = doc.getString("operatorId") ?: "",
                            lineNumber = doc.getString("lineNumber") ?: "",
                            lineName = doc.getString("lineName") ?: "",
                            lineType = try { LineType.valueOf(doc.getString("lineType") ?: "BUS") } catch (e: Exception) { LineType.BUS },
                            startStop = doc.getString("startStop") ?: "",
                            endStop = doc.getString("endStop") ?: "",
                            isActive = doc.getBoolean("isActive") ?: true,
                            averageRating = doc.getDouble("averageRating")?.toFloat() ?: 0f,
                            ratingCount = doc.getLong("ratingCount")?.toInt() ?: 0,
                            priceOneWay = doc.getDouble("priceOneWay")?.toFloat() ?: 50f,
                            priceReturn = doc.getDouble("priceReturn")?.toFloat() ?: 90f
                        )
                    } catch (e: Exception) { null }
                }
                _lines.value = lines
            } catch (e: Exception) { }
        }
    }

}