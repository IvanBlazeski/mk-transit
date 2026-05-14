package mk.fikt.mktransit.ui.screens.tickets

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import mk.fikt.mktransit.R
import mk.fikt.mktransit.viewmodel.TicketState
import mk.fikt.mktransit.viewmodel.TicketViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRTicketScreen(
    ticketId: String,
    onBack: () -> Unit,
    viewModel: TicketViewModel = hiltViewModel()
) {
    val ticketState by viewModel.ticketState.collectAsStateWithLifecycle()

    LaunchedEffect(ticketId) {
        viewModel.loadTicketById(ticketId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.your_ticket)) },
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
        when (val state = ticketState) {
            is TicketState.SingleTicket -> {
                val ticket = state.ticket
                val qrBitmap = remember(ticket.qrContent) {
                    generateQRCode(ticket.qrContent)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                color = when (ticket.status) {
                                    "PAID" -> Color(0xFF4CAF50)
                                    "USED" -> Color(0xFF9E9E9E)
                                    else -> Color(0xFFFF9800)
                                },
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = when (ticket.status) {
                                        "PAID" -> stringResource(R.string.ticket_valid)
                                        "USED" -> stringResource(R.string.ticket_used)
                                        "EXPIRED" -> stringResource(R.string.ticket_expired)
                                        else -> ticket.status
                                    },
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Line ${ticket.lineNumber}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = ticket.lineName,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = ticket.ticketType.replace("_", " "),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(24.dp))

                            qrBitmap?.let { bitmap ->
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Box(
                                        modifier = Modifier
                                            .size(200.dp)
                                            .background(Color.White, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "QR Code",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.ticket_id),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = ticket.ticketId.take(8) + "...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "MKD",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "${ticket.pricePaid.toInt()} MKD",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.purchased_at),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(text = formatDate(ticket.purchasedAt), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = stringResource(R.string.valid_until),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = formatDate(ticket.validUntil),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.QrCode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.show_qr_to_driver),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            is TicketState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.ticket_not_found))
                }
            }
        }
    }
}

fun generateQRCode(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) { null }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}