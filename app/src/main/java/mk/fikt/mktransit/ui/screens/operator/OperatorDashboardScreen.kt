package mk.fikt.mktransit.ui.screens.operator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.fikt.mktransit.R
import mk.fikt.mktransit.domain.model.BusLine
import mk.fikt.mktransit.domain.model.LineType
import mk.fikt.mktransit.viewmodel.OperatorState
import mk.fikt.mktransit.viewmodel.OperatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorDashboardScreen(
    onBack: () -> Unit,
    onDriverMode: () -> Unit,
    viewModel: OperatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val lines by viewModel.lines.collectAsStateWithLifecycle()

    var showCreateLineDialog by remember { mutableStateOf(false) }
    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showStopsDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.loadOperatorProfile() }

    LaunchedEffect(state) {
        when (state) {
            is OperatorState.SaveSuccess -> {
                showCreateLineDialog = false
                showCreateProfileDialog = false
                viewModel.loadOperatorProfile()
            }
            else -> {}
        }
    }

    if (showCreateProfileDialog) {
        CreateProfileDialog(
            onDismiss = { showCreateProfileDialog = false },
            onSave = { name, desc, email, phone, area ->
                viewModel.saveOperatorProfile(name, desc, email, phone, area)
            }
        )
    }

    if (showCreateLineDialog) {
        CreateLineDialog(
            onDismiss = { showCreateLineDialog = false },
            onCreate = { number, name, type, start, end, priceOneWay, priceReturn ->
                viewModel.createLine(number, name, type, start, end, priceOneWay, priceReturn)
            }
        )
    }

    showDeleteDialog?.let { lineId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.delete_line)) },
            text = { Text(stringResource(R.string.delete_line_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteLine(lineId)
                    showDeleteDialog = null
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    showStopsDialog?.let { lineId ->
        StopsDialog(
            lineId = lineId,
            viewModel = viewModel,
            onDismiss = { showStopsDialog = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.operator_dashboard)) },
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
        },
        floatingActionButton = {
            if (profile != null) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallFloatingActionButton(onClick = onDriverMode, containerColor = MaterialTheme.colorScheme.secondary) {
                        Icon(Icons.Filled.DirectionsBus, contentDescription = stringResource(R.string.driver_mode), tint = MaterialTheme.colorScheme.onSecondary)
                    }
                    FloatingActionButton(onClick = { showCreateLineDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_bus_line), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (profile == null) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Business, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = stringResource(R.string.setup_profile), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = stringResource(R.string.setup_profile_subtitle), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { showCreateProfileDialog = true }, shape = RoundedCornerShape(12.dp)) {
                                Text(stringResource(R.string.create_profile))
                            }
                        }
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(52.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Business, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = profile!!.companyName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = profile!!.coverageArea, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { showCreateProfileDialog = true }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            if (profile != null) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(modifier = Modifier.weight(1f), title = stringResource(R.string.total_lines), value = lines.size.toString(), icon = Icons.Filled.DirectionsBus)
                        StatCard(modifier = Modifier.weight(1f), title = stringResource(R.string.active_lines), value = lines.count { it.isActive }.toString(), icon = Icons.Filled.CheckCircle)
                    }
                }

                item { Text(text = stringResource(R.string.your_lines), fontWeight = FontWeight.Bold, fontSize = 18.sp) }

                if (lines.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.DirectionsBus, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = stringResource(R.string.no_lines_yet), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(text = stringResource(R.string.tap_to_add), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }
                        }
                    }
                } else {
                    items(lines) { line ->
                        OperatorLineCard(
                            line = line,
                            onDelete = { showDeleteDialog = line.lineId },
                            onManageStops = {
                                showStopsDialog = line.lineId
                                viewModel.loadStops(line.lineId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StopsDialog(lineId: String, viewModel: OperatorViewModel, onDismiss: () -> Unit) {
    val stops by viewModel.stops.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var newStopName by remember { mutableStateOf("") }
    var newStopMinutes by remember { mutableStateOf("") }
    var newStopLocation by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manage_stops)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (stops.isEmpty()) {
                    Text(stringResource(R.string.no_stops), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                } else {
                    stops.forEach { stop ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = stop.stopName, fontWeight = FontWeight.Medium)
                                if (stop.minutesFromStart > 0) {
                                    Text(text = "+${stop.minutesFromStart} min", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                            IconButton(onClick = { viewModel.deleteStop(lineId, stop.stopId) }) {
                                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                        HorizontalDivider()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.add_stop), fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = newStopName, onValueChange = { newStopName = it }, label = { Text(stringResource(R.string.stop_name)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = newStopLocation, onValueChange = { newStopLocation = it }, label = { Text("Локација (пр. Skopje, Centar)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = newStopMinutes, onValueChange = { newStopMinutes = it }, label = { Text(stringResource(R.string.minutes_from_start)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Button(
                    onClick = {
                        if (newStopName.isNotBlank()) {
                            viewModel.addStopWithGeocoding(context = context, lineId = lineId, stopName = newStopName, locationText = newStopLocation.ifBlank { newStopName }, minutesFromStart = newStopMinutes.toIntOrNull() ?: 0)
                            newStopName = ""; newStopMinutes = ""; newStopLocation = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_stop))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.save)) } }
    )
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun OperatorLineCard(line: BusLine, onDelete: () -> Unit, onManageStops: () -> Unit = {}) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = line.lineNumber, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = line.lineName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(text = "${line.startStop} → ${line.endStop}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                if (line.priceOneWay > 0) {
                    Text(
                        text = "${line.priceOneWay.toInt()} MKD / ${line.priceReturn.toInt()} MKD",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Surface(
                color = if (line.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (line.isActive) stringResource(R.string.active) else stringResource(R.string.inactive),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    color = if (line.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onManageStops) {
                Icon(Icons.Filled.Place, contentDescription = "Stops", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun CreateProfileDialog(onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var companyName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.company_profile)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = companyName, onValueChange = { companyName = it }, label = { Text(stringResource(R.string.company_name)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(R.string.description)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.contact_email)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(stringResource(R.string.phone)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text(stringResource(R.string.coverage_area)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = { if (companyName.isNotBlank()) onSave(companyName, description, email, phone, area) }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLineDialog(onDismiss: () -> Unit, onCreate: (String, String, LineType, String, String, Float, Float) -> Unit) {
    var lineNumber by remember { mutableStateOf("") }
    var lineName by remember { mutableStateOf("") }
    var lineType by remember { mutableStateOf(LineType.BUS) }
    var startStop by remember { mutableStateOf("") }
    var endStop by remember { mutableStateOf("") }
    var priceOneWay by remember { mutableStateOf("") }
    var priceReturn by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_bus_line)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = lineNumber, onValueChange = { lineNumber = it }, label = { Text(stringResource(R.string.line_number)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = lineName, onValueChange = { lineName = it }, label = { Text(stringResource(R.string.line_name)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = lineType.name, onValueChange = {}, readOnly = true,
                        label = { Text(stringResource(R.string.line_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        LineType.entries.forEach { type ->
                            DropdownMenuItem(text = { Text(type.name) }, onClick = { lineType = type; expanded = false })
                        }
                    }
                }
                OutlinedTextField(value = startStop, onValueChange = { startStop = it }, label = { Text(stringResource(R.string.start_stop)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = endStop, onValueChange = { endStop = it }, label = { Text(stringResource(R.string.end_stop)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)

                HorizontalDivider()
                Text("Цени (MKD)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                OutlinedTextField(
                    value = priceOneWay,
                    onValueChange = { priceOneWay = it },
                    label = { Text("Цена - Во еден правец") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = priceReturn,
                    onValueChange = { priceReturn = it },
                    label = { Text("Цена - Повратна") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (lineNumber.isNotBlank() && lineName.isNotBlank()) {
                    onCreate(
                        lineNumber, lineName, lineType, startStop, endStop,
                        priceOneWay.toFloatOrNull() ?: 50f,
                        priceReturn.toFloatOrNull() ?: 90f
                    )
                }
            }) { Text(stringResource(R.string.create)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}