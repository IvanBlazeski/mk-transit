package mk.fikt.mktransit.ui.screens.lines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import mk.fikt.mktransit.domain.model.Rating
import mk.fikt.mktransit.domain.model.Stop
import mk.fikt.mktransit.viewmodel.LineDetailState
import mk.fikt.mktransit.viewmodel.LineDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineDetailScreen(
    lineId: String,
    onBack: () -> Unit,
    onBuyTicket: (String) -> Unit,
    viewModel: LineDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedStars by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(lineId) {
        viewModel.loadLineDetail(lineId)
    }

    if (showRatingDialog) {
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text(stringResource(R.string.rate_line)) },
            text = {
                Column {
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { selectedStars = star }) {
                                Icon(
                                    if (star <= selectedStars) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text(stringResource(R.string.your_comment)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedStars > 0) {
                            viewModel.submitRating(lineId, selectedStars, comment)
                            showRatingDialog = false
                        }
                    }
                ) { Text(stringResource(R.string.submit_rating)) }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val s = state) {
                        is LineDetailState.Success -> Text("Line ${s.line.lineNumber}")
                        else -> Text(stringResource(R.string.line_detail_title))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (state is LineDetailState.Success) {
                            val line = (state as LineDetailState.Success).line
                            viewModel.toggleFavorite(lineId, line.lineName)
                        }
                    }) {
                        Icon(
                            if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(R.string.favorites),
                            tint = if (isFavorite) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onPrimary
                        )
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
        when (val s = state) {
            is LineDetailState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is LineDetailState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is LineDetailState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = s.line.lineNumber,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = s.line.lineName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Text(text = s.line.lineType.name, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (s.line.ratingCount > 0)
                                            "%.1f (${s.line.ratingCount})".format(s.line.averageRating)
                                        else stringResource(R.string.no_reviews),
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    TextButton(onClick = { showRatingDialog = true }) {
                                        Text(stringResource(R.string.rate_line))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { onBuyTicket(lineId) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.ConfirmationNumber, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.buy_ticket), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    item {
                        Text(
                            text = "${stringResource(R.string.stops)} (${s.stops.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    if (s.stops.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Text(
                                    text = stringResource(R.string.no_stops),
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        itemsIndexed(s.stops) { index, stop ->
                            StopItem(stop = stop, isFirst = index == 0, isLast = index == s.stops.lastIndex)
                        }
                    }

                    item {
                        Text(
                            text = "${stringResource(R.string.reviews)} (${s.ratings.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    if (s.ratings.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Text(
                                    text = stringResource(R.string.no_reviews),
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        items(s.ratings.size) { index ->
                            RatingItem(rating = s.ratings[index])
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StopItem(stop: Stop, isFirst: Boolean, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            if (!isFirst) {
                Box(modifier = Modifier.width(2.dp).height(12.dp)) { HorizontalDivider() }
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = when {
                    isFirst -> MaterialTheme.colorScheme.primary
                    isLast -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primaryContainer
                },
                modifier = Modifier.size(12.dp)
            ) {}
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).height(12.dp)) { HorizontalDivider() }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stop.stopName, fontWeight = if (isFirst || isLast) FontWeight.Bold else FontWeight.Normal)
                    if (stop.minutesFromStart > 0) {
                        Text(text = "+${stop.minutesFromStart} min", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                if (isFirst) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                        Text("START", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (isLast) {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
                        Text("END", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun RatingItem(rating: Rating) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    Icon(
                        if (index < rating.stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (rating.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = rating.comment, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
        }
    }
}