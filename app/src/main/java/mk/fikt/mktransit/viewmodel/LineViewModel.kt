package mk.fikt.mktransit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mk.fikt.mktransit.domain.model.BusLine
import mk.fikt.mktransit.domain.model.LineType
import javax.inject.Inject

sealed class LineState {
    object Loading : LineState()
    data class Success(val lines: List<BusLine>) : LineState()
    data class Error(val message: String) : LineState()
}

@HiltViewModel
class LineViewModel @Inject constructor() : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _lineState = MutableStateFlow<LineState>(LineState.Loading)
    val lineState: StateFlow<LineState> = _lineState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        loadLines()
    }

    fun loadLines() {
        viewModelScope.launch {
            _lineState.value = LineState.Loading
            try {
                val snapshot = firestore.collection("lines")
                    .get().await()

                android.util.Log.d("LineViewModel", "Total docs: ${snapshot.documents.size}")

                val lines = snapshot.documents.mapNotNull { doc ->
                    android.util.Log.d("LineViewModel", "Doc ID: ${doc.id}")
                    android.util.Log.d("LineViewModel", "Data: ${doc.data}")
                    try {
                        BusLine(
                            lineId = doc.id,
                            operatorId = doc.getString("operatorId") ?: "",
                            lineNumber = doc.getString("lineNumber") ?: "",
                            lineName = doc.getString("lineName")
                                ?: doc.getString("LineName")
                                ?: doc.getString("linename")
                                ?: "",
                            lineType = try {
                                LineType.valueOf(doc.getString("lineType") ?: "BUS")
                            } catch (e: Exception) {
                                LineType.BUS
                            },
                            startStop = doc.getString("startStop") ?: "",
                            endStop = doc.getString("endStop") ?: "",
                            isActive = doc.getBoolean("isActive") ?: true,
                            averageRating = doc.getDouble("averageRating")?.toFloat() ?: 0f,
                            ratingCount = doc.getLong("ratingCount")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("LineViewModel", "Error parsing doc: ${e.message}")
                        null
                    }
                }
                android.util.Log.d("LineViewModel", "Parsed lines: ${lines.size}")
                _lineState.value = LineState.Success(lines)
            } catch (e: Exception) {
                android.util.Log.e("LineViewModel", "Error: ${e.message}")
                _lineState.value = LineState.Error(e.message ?: "Failed to load lines")
            }
        }
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredLines(lines: List<BusLine>): List<BusLine> {
        val query = _searchQuery.value.lowercase()
        if (query.isBlank()) return lines
        return lines.filter {
            it.lineNumber.lowercase().contains(query) ||
                    it.lineName.lowercase().contains(query) ||
                    it.startStop.lowercase().contains(query) ||
                    it.endStop.lowercase().contains(query)
        }
    }
}