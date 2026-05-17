package mk.fikt.mktransit.ui.screens.tickets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mk.fikt.mktransit.R
import mk.fikt.mktransit.viewmodel.TicketState
import mk.fikt.mktransit.viewmodel.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketPurchaseScreen(
    lineId: String,
    lineName: String = "",
    lineNumber: String = "",
    priceOneWay: Float = 50f,
    priceReturn: Float = 90f,
    onBack: () -> Unit,
    onPurchaseSuccess: (String) -> Unit,
    viewModel: TicketViewModel = hiltViewModel()
) {
    val ticketState by viewModel.ticketState.collectAsStateWithLifecycle()
    var selectedType by remember { mutableStateOf("ONE_WAY") }
    var quantity by remember { mutableStateOf(1) }

    // Вчитај податоци за линијата од Firestore
    var actualLineName by remember { mutableStateOf(lineName) }
    var actualLineNumber by remember { mutableStateOf(lineNumber) }
    var actualPriceOneWay by remember { mutableStateOf(priceOneWay) }
    var actualPriceReturn by remember { mutableStateOf(priceReturn) }

    LaunchedEffect(lineId) {
        try {
            val doc = FirebaseFirestore.getInstance()
                .collection("lines").document(lineId).get().await()
            actualLineName = doc.getString("lineName") ?: lineName
            actualLineNumber = doc.getString("lineNumber") ?: lineNumber
            actualPriceOneWay = doc.getDouble("priceOneWay")?.toFloat() ?: priceOneWay
            actualPriceReturn = doc.getDouble("priceReturn")?.toFloat() ?: priceReturn
        } catch (e: Exception) { }
    }

    LaunchedEffect(ticketState) {
        if (ticketState is TicketState.PurchaseSuccess) {
            onPurchaseSuccess((ticketState as TicketState.PurchaseSuccess).ticketId)
        }
    }

    val ticketTypes = listOf(
        Triple("ONE_WAY", "Во еден правец", actualPriceOneWay),
        Triple("RETURN", "Повратна карта", actualPriceReturn)
    )

    val selectedPrice = ticketTypes.find { it.first == selectedType }?.third ?: actualPriceOneWay
    val totalPrice = selectedPrice * quantity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.buy_ticket_title)) },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = actualLineNumber, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = actualLineName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = stringResource(R.string.buy_ticket_title), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Тип на карта", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))

            ticketTypes.forEach { (type, label, price) ->
                TicketTypeCard(
                    type = type,
                    label = label,
                    price = price,
                    isSelected = selectedType == type,
                    onClick = { selectedType = type }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Број на карти", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { if (quantity > 1) quantity-- }, enabled = quantity > 1) {
                        Icon(Icons.Filled.Remove, contentDescription = "Намали", tint = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$quantity", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "карти", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    IconButton(onClick = { if (quantity < 10) quantity++ }, enabled = quantity < 10) {
                        Icon(Icons.Filled.Add, contentDescription = "Зголеми", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (ticketState is TicketState.Error) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth()) {
                    Text(text = (ticketState as TicketState.Error).message, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Цена по карта:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(text = "${selectedPrice.toInt()} MKD", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Количина:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(text = "x$quantity", fontSize = 14.sp)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Вкупно:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "${totalPrice.toInt()} MKD", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.purchaseTicket(
                        lineId = lineId,
                        lineName = actualLineName,
                        lineNumber = actualLineNumber,
                        ticketType = selectedType,
                        price = totalPrice,
                        quantity = quantity
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = ticketState !is TicketState.Loading
            ) {
                if (ticketState is TicketState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Filled.ConfirmationNumber, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.purchase), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun TicketTypeCard(type: String, label: String, price: Float, isSelected: Boolean, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (type == "ONE_WAY") Icons.Filled.ArrowForward else Icons.Filled.SyncAlt,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                Text(text = if (type == "ONE_WAY") "Еднонасочно патување" else "Таму и назад", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Text(text = "${price.toInt()} MKD", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        }
    }
}