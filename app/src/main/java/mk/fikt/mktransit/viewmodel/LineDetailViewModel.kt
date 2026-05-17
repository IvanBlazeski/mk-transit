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
import mk.fikt.mktransit.domain.model.Rating
import mk.fikt.mktransit.domain.model.Stop
import javax.inject.Inject

sealed class LineDetailState {
    object Loading : LineDetailState()
    data class Success(
        val line: BusLine,
        val stops: List<Stop>,
        val ratings: List<Rating>
    ) : LineDetailState()
    data class Error(val message: String) : LineDetailState()
}

@HiltViewModel
class LineDetailViewModel @Inject constructor() : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<LineDetailState>(LineDetailState.Loading)
    val state: StateFlow<LineDetailState> = _state

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val _ratingSubmitted = MutableStateFlow(false)
    val ratingSubmitted: StateFlow<Boolean> = _ratingSubmitted

    fun loadLineDetail(lineId: String) {
        viewModelScope.launch {
            _state.value = LineDetailState.Loading
            try {
                val lineDoc = firestore.collection("lines")
                    .document(lineId).get().await()

                // Земи го operatorId (Document ID во operators колекцијата)
                val operatorProfileId = lineDoc.getString("operatorId") ?: ""

                // Преведи го во вистинскиот Firebase Auth UID
                var operatorUid = operatorProfileId
                try {
                    if (operatorProfileId.isNotBlank()) {
                        val operatorDoc = firestore.collection("operators")
                            .document(operatorProfileId)
                            .get().await()
                        if (operatorDoc.exists()) {
                            operatorUid = operatorDoc.getString("uid") ?: operatorProfileId
                        }
                    }
                } catch (e: Exception) { }

                val line = BusLine(
                    lineId = lineDoc.id,
                    operatorId = operatorUid, // ← Вистинскиот Auth UID
                    lineNumber = lineDoc.getString("lineNumber") ?: "",
                    lineName = lineDoc.getString("lineName")
                        ?: lineDoc.getString("LineName")
                        ?: lineDoc.getString("linename") ?: "",
                    lineType = try {
                        LineType.valueOf(lineDoc.getString("lineType") ?: "BUS")
                    } catch (e: Exception) { LineType.BUS },
                    startStop = lineDoc.getString("startStop") ?: "",
                    endStop = lineDoc.getString("endStop") ?: "",
                    isActive = lineDoc.getBoolean("isActive") ?: true,
                    averageRating = lineDoc.getDouble("averageRating")?.toFloat() ?: 0f,
                    ratingCount = lineDoc.getLong("ratingCount")?.toInt() ?: 0
                )

                val stopsSnapshot = firestore.collection("lines")
                    .document(lineId)
                    .collection("stops")
                    .orderBy("stopOrder")
                    .get().await()

                val stops = stopsSnapshot.documents.map { doc ->
                    Stop(
                        stopId = doc.id,
                        stopName = doc.getString("stopName") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        stopOrder = doc.getLong("stopOrder")?.toInt() ?: 0,
                        minutesFromStart = doc.getLong("minutesFromStart")?.toInt() ?: 0
                    )
                }

                val ratingsSnapshot = firestore.collection("lines")
                    .document(lineId)
                    .collection("ratings")
                    .get().await()

                val ratings = ratingsSnapshot.documents.map { doc ->
                    Rating(
                        ratingId = doc.id,
                        userId = doc.getString("userId") ?: "",
                        stars = doc.getLong("stars")?.toInt() ?: 0,
                        comment = doc.getString("comment") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }

                _state.value = LineDetailState.Success(line, stops, ratings)
                checkFavorite(lineId)

            } catch (e: Exception) {
                _state.value = LineDetailState.Error(e.message ?: "Failed to load line")
            }
        }
    }

    fun submitRating(lineId: String, stars: Int, comment: String) {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val rating = hashMapOf(
                    "userId" to uid,
                    "stars" to stars,
                    "comment" to comment,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("lines")
                    .document(lineId)
                    .collection("ratings")
                    .add(rating).await()

                _ratingSubmitted.value = true
                loadLineDetail(lineId)
            } catch (e: Exception) { }
        }
    }

    fun toggleFavorite(lineId: String, lineName: String) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            try {
                if (_isFavorite.value) {
                    val favSnapshot = firestore.collection("favorites")
                        .whereEqualTo("userId", uid)
                        .whereEqualTo("lineId", lineId)
                        .get().await()
                    favSnapshot.documents.forEach { it.reference.delete().await() }
                    _isFavorite.value = false
                } else {
                    val fav = hashMapOf(
                        "userId" to uid,
                        "lineId" to lineId,
                        "lineName" to lineName,
                        "notifyMinutesBefore" to 10
                    )
                    firestore.collection("favorites").add(fav).await()
                    _isFavorite.value = true
                }
            } catch (e: Exception) { }
        }
    }

    private suspend fun checkFavorite(lineId: String) {
        val uid = auth.currentUser?.uid ?: return
        val snapshot = firestore.collection("favorites")
            .whereEqualTo("userId", uid)
            .whereEqualTo("lineId", lineId)
            .get().await()
        _isFavorite.value = !snapshot.isEmpty
    }
}