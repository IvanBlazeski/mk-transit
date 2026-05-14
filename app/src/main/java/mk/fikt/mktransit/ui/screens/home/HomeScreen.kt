package mk.fikt.mktransit.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import mk.fikt.mktransit.domain.model.BusLine
import mk.fikt.mktransit.domain.model.LineType
import mk.fikt.mktransit.viewmodel.LineState
import mk.fikt.mktransit.viewmodel.LineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLineClick: (String) -> Unit,
    onMapClick: () -> Unit,
    onTicketsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onProfileClick: () -> Unit,
    isTablet: Boolean = false
) {
    val lineViewModel: LineViewModel = hiltViewModel()
    val lineState by lineViewModel.lineState.collectAsStateWithLifecycle()
    val searchQuery by lineViewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Filled.Person, contentDescription = stringResource(R.string.nav_profile))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_home)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onMapClick,
                    icon = { Icon(Icons.Filled.Map, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_map)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onTicketsClick,
                    icon = { Icon(Icons.Filled.ConfirmationNumber, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_tickets)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onMessagesClick,
                    icon = { Icon(Icons.Filled.Message, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_messages)) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { lineViewModel.updateSearch(it) },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            when (val state = lineState) {
                is LineState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                is LineState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.WifiOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.could_not_load),
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { lineViewModel.loadLines() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }

                is LineState.Success -> {
                    val filtered = lineViewModel.getFilteredLines(state.lines)

                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.no_lines_found),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        if (isTablet) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item(span = { GridItemSpan(2) }) {
                                    Text(
                                        text = stringResource(R.string.bus_lines),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(filtered) { line ->
                                    BusLineCard(
                                        line = line,
                                        onClick = { onLineClick(line.lineId) }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Text(
                                        text = stringResource(R.string.bus_lines),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(filtered) { line ->
                                    BusLineCard(
                                        line = line,
                                        onClick = { onLineClick(line.lineId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BusLineCard(
    line: BusLine,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        text = line.lineNumber,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (line.lineNumber.length > 3) 12.sp else 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = line.lineName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.TripOrigin,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${line.startStop} → ${line.endStop}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                if (line.ratingCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f (${line.ratingCount})".format(line.averageRating),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Surface(
                color = when (line.lineType) {
                    LineType.BUS -> MaterialTheme.colorScheme.primaryContainer
                    LineType.MINIBUS -> MaterialTheme.colorScheme.secondaryContainer
                    LineType.TROLLEY -> MaterialTheme.colorScheme.tertiaryContainer
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = line.lineType.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}