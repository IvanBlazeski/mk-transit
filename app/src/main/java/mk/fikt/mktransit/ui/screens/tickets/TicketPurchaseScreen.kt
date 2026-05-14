package mk.fikt.mktransit.ui.screens.tickets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import mk.fikt.mktransit.R
import mk.fikt.mktransit.viewmodel.TicketState
import mk.fikt.mktransit.viewmodel.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketPurchaseScreen(
    lineId: String,
    lineName: String,
    lineNumber: String,
    onBack: () -> Unit,
    onPurchaseSuccess: (String) -> Unit,
    viewModel: TicketViewModel = hiltViewModel()
) {
    val ticketState by viewModel.ticketState.collectAsStateWithLifecycle()
    var selectedType by remember { mutableStateOf("SINGLE") }

    LaunchedEffect(ticketState) {
        if (ticketState is TicketState.PurchaseSuccess) {
            onPurchaseSuccess((ticketState as TicketState.PurchaseSuccess).ticketId)
        }
    }

    val ticketTypes = listOf(
        Triple("SINGLE", stringResource(R.string.ticket_single), 35f),
        Triple("DAILY", stringResource(R.string.ticket_daily), 120f),
        Triple("WEEKLY", stringResource(R.string.ticket_weekly), 600f)
    )

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = lineNumber,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = lineName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = stringResource(R.string.buy_ticket_title),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.buy_ticket_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.weight(1f))

            if (ticketState is TicketState.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (ticketState as TicketState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            val selectedPrice = ticketTypes.find { it.first == selectedType }?.third ?: 35f
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "${selectedPrice.toInt()} MKD",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.purchaseTicket(
                        lineId = lineId,
                        lineName = lineName,
                        lineNumber = lineNumber,
                        ticketType = selectedType,
                        price = selectedPrice
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
fun TicketTypeCard(
    type: String,
    label: String,
    price: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${price.toInt()} MKD",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}