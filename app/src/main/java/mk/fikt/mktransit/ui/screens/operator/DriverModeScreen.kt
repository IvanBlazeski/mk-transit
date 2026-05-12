package mk.fikt.mktransit.ui.screens.operator

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.tasks.await
import mk.fikt.mktransit.viewmodel.OperatorViewModel

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverModeScreen(
    onBack: () -> Unit,
    onScanQR: () -> Unit,
    viewModel: OperatorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val lines by viewModel.lines.collectAsStateWithLifecycle()

    var isActive by remember { mutableStateOf(false) }
    var selectedLineId by remember { mutableStateOf("") }
    var selectedLineName by remember { mutableStateOf("Select a line") }
    var currentLat by remember { mutableStateOf(0.0) }
    var currentLon by remember { mutableStateOf(0.0) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadOperatorProfile()
    }

    // GPS споделување секои 5 секунди
    LaunchedEffect(isActive, selectedLineId) {
        if (isActive && selectedLineId.isNotBlank()) {
            while (this.isActive) {
                try {
                    val fusedClient = LocationServices
                        .getFusedLocationProviderClient(context)
                    val location = fusedClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY, null
                    ).await()

                    location?.let { loc ->
                        currentLat = loc.latitude
                        currentLon = loc.longitude

                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        FirebaseFirestore.getInstance()
                            .collection("vehicleLocations")
                            .document(selectedLineId)
                            .set(
                                hashMapOf(
                                    "lineId" to selectedLineId,
                                    "driverUid" to uid,
                                    "latitude" to loc.latitude,
                                    "longitude" to loc.longitude,
                                    "bearing" to (location.bearing),
                                    "updatedAt" to System.currentTimeMillis(),
                                    "isActive" to true
                                )
                            )
                    }
                } catch (e: Exception) {
                    // ignore
                }
                delay(5000)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Mode") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                if (isActive) Color.White.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isActive) Icons.Filled.DirectionsBus else Icons.Filled.DirectionsBus,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = if (isActive) Color.White else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isActive) "DRIVING" else "OFFLINE",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isActive) {
                        Text(
                            text = selectedLineName,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "GPS: %.4f, %.4f".format(currentLat, currentLon),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Line Selector
            if (!isActive) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedLineName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Line") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        lines.forEach { line ->
                            DropdownMenuItem(
                                text = { Text("${line.lineNumber} - ${line.lineName}") },
                                onClick = {
                                    selectedLineId = line.lineId
                                    selectedLineName = "${line.lineNumber} - ${line.lineName}"
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Start/Stop Button
            Button(
                onClick = {
                    if (selectedLineId.isNotBlank()) {
                        isActive = !isActive
                        if (!isActive) {
                            // Деактивирај
                            FirebaseFirestore.getInstance()
                                .collection("vehicleLocations")
                                .document(selectedLineId)
                                .update("isActive", false)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.error
                    else Color(0xFF4CAF50)
                ),
                enabled = selectedLineId.isNotBlank() || isActive
            ) {
                Icon(
                    if (isActive) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isActive) "Stop Driving" else "Start Driving",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider()

            // QR Scanner Button
            Text(
                text = "Ticket Validation",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            OutlinedButton(
                onClick = onScanQR,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Scan QR Ticket",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GPS location is shared every 5 seconds while driving",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}