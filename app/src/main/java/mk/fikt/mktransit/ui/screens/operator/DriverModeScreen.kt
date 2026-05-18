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
import androidx.compose.ui.res.stringResource
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
import mk.fikt.mktransit.R
import mk.fikt.mktransit.domain.model.UserRole
import mk.fikt.mktransit.viewmodel.AuthState
import mk.fikt.mktransit.viewmodel.AuthViewModel
import mk.fikt.mktransit.viewmodel.OperatorViewModel

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverModeScreen(
    onBack: () -> Unit,
    onScanQR: () -> Unit,
    isStandaloneDriver: Boolean = false,
    viewModel: OperatorViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lines by viewModel.lines.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    val isDriverAccount = authState is AuthState.Success &&
            (authState as AuthState.Success).user.role == UserRole.DRIVER

    var isActive by remember { mutableStateOf(false) }
    var selectedLineId by remember { mutableStateOf("") }
    var selectedLineName by remember { mutableStateOf("") }
    var currentLat by remember { mutableStateOf(0.0) }
    var currentLon by remember { mutableStateOf(0.0) }
    var expanded by remember { mutableStateOf(false) }

    val selectLineText = stringResource(R.string.select_line)

    LaunchedEffect(Unit) {
        selectedLineName = selectLineText
        viewModel.loadAllLines()
    }

    LaunchedEffect(isActive, selectedLineId) {
        if (isActive && selectedLineId.isNotBlank()) {
            while (this.isActive) {
                try {
                    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
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
                            .set(hashMapOf(
                                "lineId" to selectedLineId,
                                "driverUid" to uid,
                                "latitude" to loc.latitude,
                                "longitude" to loc.longitude,
                                "bearing" to (location.bearing),
                                "updatedAt" to System.currentTimeMillis(),
                                "isActive" to true
                            ))
                    }
                } catch (e: Exception) { }
                delay(5000)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.driver_mode),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    if (!isStandaloneDriver) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                actions = {
                    if (isStandaloneDriver) {
                        IconButton(onClick = { authViewModel.logout(context) }) {
                            Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
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
                            Icons.Filled.DirectionsBus,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = if (isActive) Color.White else MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isActive) stringResource(R.string.driving) else stringResource(R.string.offline),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = selectedLineName, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "GPS: %.4f, %.4f".format(currentLat, currentLon), fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                    }
                }
            }

            if (!isActive) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedLineName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.select_line)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

            Button(
                onClick = {
                    if (selectedLineId.isNotBlank()) {
                        val newIsActive = !isActive
                        isActive = newIsActive

                        if (newIsActive) {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            val db = FirebaseFirestore.getInstance()

                            // Зачувај нотификација
                            val notif = hashMapOf(
                                "lineId" to selectedLineId,
                                "lineName" to selectedLineName,
                                "message" to "Автобусот за $selectedLineName тргна!",
                                "driverUid" to uid,
                                "timestamp" to System.currentTimeMillis(),
                                "isRead" to false
                            )
                            db.collection("lineNotifications").add(notif)
                                .addOnSuccessListener {
                                    android.util.Log.d("Driver", "Notification saved!")
                                }
                                .addOnFailureListener { e ->
                                    android.util.Log.e("Driver", "Error: ${e.message}")
                                }
                        } else {
                            FirebaseFirestore.getInstance()
                                .collection("vehicleLocations")
                                .document(selectedLineId)
                                .update("isActive", false)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
                ),
                enabled = selectedLineId.isNotBlank() || isActive
            ) {
                Icon(if (isActive) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isActive) stringResource(R.string.stop_driving) else stringResource(R.string.start_driving), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            HorizontalDivider()

            Text(text = stringResource(R.string.ticket_validation), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.Start))

            OutlinedButton(onClick = onScanQR, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.scan_qr), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.gps_info), fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Start)
                }
            }
        }
    }
}