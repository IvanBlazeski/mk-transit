package mk.fikt.mktransit.ui.screens.tickets

import android.Manifest
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import mk.fikt.mktransit.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QRScannerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    var scanResult by remember { mutableStateOf("") }
    var ticketStatus by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf<Boolean?>(null) }
    var isScanning by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_qr_title)) },
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
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!cameraPermission.status.isGranted) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.camera_permission_needed))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                            Text(stringResource(R.string.enable_camera))
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    if (isScanning) {
                        AndroidView(
                            factory = { ctx ->
                                DecoratedBarcodeView(ctx).apply {
                                    decodeContinuous(object : BarcodeCallback {
                                        override fun barcodeResult(result: BarcodeResult) {
                                            if (result.text != scanResult) {
                                                scanResult = result.text
                                                isScanning = false
                                                pause()
                                                scope.launch {
                                                    validateTicket(result.text) { valid, status ->
                                                        isValid = valid
                                                        ticketStatus = status
                                                    }
                                                }
                                            }
                                        }
                                    })
                                    resume()
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isValid != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isValid == true) Color(0xFF4CAF50)
                            else MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                if (isValid == true) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = if (isValid == true) Color.White else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isValid == true) stringResource(R.string.valid_ticket)
                                else stringResource(R.string.invalid_ticket),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isValid == true) Color.White else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = ticketStatus,
                                fontSize = 14.sp,
                                color = if (isValid == true) Color.White.copy(alpha = 0.9f)
                                else MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isValid = null
                            ticketStatus = ""
                            scanResult = ""
                            isScanning = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.scan_another))
                    }
                } else if (isScanning) {
                    Text(
                        text = stringResource(R.string.point_camera),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

suspend fun validateTicket(
    qrContent: String,
    onResult: (Boolean, String) -> Unit
) {
    try {
        val firestore = FirebaseFirestore.getInstance()
        val snapshot = firestore.collection("tickets")
            .whereEqualTo("qrContent", qrContent)
            .get().await()

        if (snapshot.isEmpty) {
            onResult(false, "Ticket not found")
            return
        }

        val doc = snapshot.documents.first()
        val status = doc.getString("status") ?: "UNKNOWN"
        val validUntil = doc.getLong("validUntil") ?: 0L

        when {
            status == "USED" -> onResult(false, "Ticket already used")
            status == "EXPIRED" -> onResult(false, "Ticket expired")
            validUntil < System.currentTimeMillis() -> onResult(false, "Ticket expired")
            else -> {
                firestore.collection("tickets")
                    .document(doc.id)
                    .update("status", "USED")
                onResult(true, "Ticket valid! Marked as used.")
            }
        }
    } catch (e: Exception) {
        onResult(false, "Error: ${e.message}")
    }
}