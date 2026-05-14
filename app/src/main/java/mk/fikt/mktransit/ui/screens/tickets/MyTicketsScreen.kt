package mk.fikt.mktransit.ui.screens.tickets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.fikt.mktransit.R
import mk.fikt.mktransit.data.local.entity.TicketEntity
import mk.fikt.mktransit.viewmodel.TicketState
import mk.fikt.mktransit.viewmodel.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    onBack: () -> Unit,
    onTicketClick: (String) -> Unit,
    viewModel: TicketViewModel = hiltViewModel()
) {
    val ticketState by viewModel.ticketState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadMyTickets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_tickets)) },
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
            is TicketState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is TicketState.Success -> {
                if (state.tickets.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.ConfirmationNumber,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.no_tickets),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.tickets) { ticket ->
                            TicketCard(
                                ticket = ticket,
                                onClick = { onTicketClick(ticket.ticketId) }
                            )
                        }
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun TicketCard(ticket: TicketEntity, onClick: () -> Unit) {
    val validText = stringResource(R.string.ticket_valid)
    val usedText = stringResource(R.string.ticket_used)
    val expiredText = stringResource(R.string.ticket_expired)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = ticket.lineNumber,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = ticket.lineName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ticket.ticketType.replace("_", " "),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(ticket.purchasedAt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = when (ticket.status) {
                        "PAID" -> Color(0xFF4CAF50)
                        "USED" -> Color(0xFF9E9E9E)
                        "EXPIRED" -> Color(0xFFE53935)
                        else -> Color(0xFFFF9800)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (ticket.status) {
                            "PAID" -> validText
                            "USED" -> usedText
                            "EXPIRED" -> expiredText
                            else -> ticket.status
                        },
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${ticket.pricePaid.toInt()} MKD",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
        }
    }
}