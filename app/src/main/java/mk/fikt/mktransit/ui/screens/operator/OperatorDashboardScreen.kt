package mk.fikt.mktransit.ui.screens.operator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
    var showPricesDialog by remember { mutableStateOf<BusLine?>(null) }
    var showScheduleDialog by remember { mutableStateOf<BusLine?>(null) }

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
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    showStopsDialog?.let { lineId ->
        StopsDialog(lineId = lineId, viewModel = viewModel, onDismiss = { showStopsDialog = null })
    }

    showPricesDialog?.let { line ->
        EditPricesDialog(
            line = line,
            onDismiss = { showPricesDialog = null },
            onSave = { p1, p2 ->
                viewModel.updateLinePrices(line.lineId, p1, p2)
                showPricesDialog = null
            }
        )
    }

    showScheduleDialog?.let { line ->
        ScheduleDialog(
            lineId = line.lineId,
            lineName = line.lineName,
            viewModel = viewModel,
            onDismiss = { showScheduleDialog = null }
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
                            },
                            onEditPrices = { showPricesDialog = line },
                            onSchedule = {
                                showScheduleDialog = line
                                viewModel.loadSchedule(line.lineId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDialog(
    lineId: String,
    lineName: String,
    viewModel: OperatorViewModel,
    onDismiss: () -> Unit
) {
    val schedule by viewModel.schedule.collectAsStateWithLifecycle()
    var direction by remember { mutableStateOf("FORWARD") }
    var departureTime by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }

    val allDays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    val dayLabels = mapOf(
        "MON" to "Пон", "TUE" to "Вто", "WED" to "Сре",
        "THU" to "Чет", "FRI" to "Пет", "SAT" to "Саб", "SUN" to "Нед"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Возен ред — $lineName") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                val forwardSchedule = schedule.filter { it.direction == "FORWARD" }
                val returnSchedule = schedule.filter { it.direction == "RETURN" }

                if (forwardSchedule.isNotEmpty()) {
                    Text("→ Во еден правец", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    forwardSchedule.forEach { s ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🕐 ${s.departureTime} — ${s.days.mapNotNull { dayLabels[it] }.joinToString(", ")}",
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.deleteSchedule(lineId, s.scheduleId) }) {
                                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                if (returnSchedule.isNotEmpty()) {
                    Text("← Повратно", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    returnSchedule.forEach { s ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🕐 ${s.departureTime} — ${s.days.mapNotNull { dayLabels[it] }.joinToString(", ")}",
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.deleteSchedule(lineId, s.scheduleId) }) {
                                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                HorizontalDivider()
                Text("Додај поаѓање", fontWeight = FontWeight.SemiBold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = direction == "FORWARD", onClick = { direction = "FORWARD" }, label = { Text("→ Напред") })
                    FilterChip(selected = direction == "RETURN", onClick = { direction = "RETURN" }, label = { Text("← Назад") })
                }

                OutlinedTextField(
                    value = departureTime,
                    onValueChange = { departureTime = it },
                    label = { Text("Час на поаѓање (пр. 08:00)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text("Денови:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    allDays.forEach { day ->
                        FilterChip(
                            selected = day in selectedDays,
                            onClick = {
                                selectedDays = if (day in selectedDays) selectedDays - day else selectedDays + day
                            },
                            label = { Text(dayLabels[day] ?: day, fontSize = 11.sp) }
                        )
                    }
                }

                Button(
                    onClick = {
                        if (departureTime.isNotBlank() && selectedDays.isNotEmpty()) {
                            viewModel.addSchedule(lineId, direction, departureTime, selectedDays.toList())
                            departureTime = ""
                            selectedDays = emptySet()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Додај поаѓање")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.save)) } }
    )
}

@Composable
fun EditPricesDialog(line: BusLine, onDismiss: () -> Unit, onSave: (Float, Float) -> Unit) {
    var p1 by remember { mutableStateOf(line.priceOneWay.toInt().toString()) }
    var p2 by remember { mutableStateOf(line.priceReturn.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ажурирај цени — ${line.lineName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = p1, onValueChange = { p1 = it }, label = { Text("Цена - Во еден правец (MKD)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = p2, onValueChange = { p2 = it }, label = { Text("Цена - Повратна (MKD)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(p1.toFloatOrNull() ?: line.priceOneWay, p2.toFloatOrNull() ?: line.priceReturn) }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
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
                                if (stop.minutesFromStart > 0) Text(text = "+${stop.minutesFromStart} min", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
fun OperatorLineCard(
    line: BusLine,
    onDelete: () -> Unit,
    onManageStops: () -> Unit = {},
    onEditPrices: () -> Unit = {},
    onSchedule: () -> Unit = {}
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Горен ред — број + ime + статус
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = line.lineNumber, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = line.lineName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "${line.startStop} → ${line.endStop}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(4.dp))

            // Долен ред — цени + акции
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Еден правец: ${line.priceOneWay.toInt()} MKD", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Повратна: ${line.priceReturn.toInt()} MKD", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                Row {
                    IconButton(onClick = onSchedule, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Schedule, contentDescription = "Возен ред", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onEditPrices, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.AttachMoney, contentDescription = "Цени", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onManageStops, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Place, contentDescription = "Стопови", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Избриши", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
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
                OutlinedTextField(value = priceOneWay, onValueChange = { priceOneWay = it }, label = { Text("Цена - Во еден правец") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = priceReturn, onValueChange = { priceReturn = it }, label = { Text("Цена - Повратна") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (lineNumber.isNotBlank() && lineName.isNotBlank()) {
                    onCreate(lineNumber, lineName, lineType, startStop, endStop, priceOneWay.toFloatOrNull() ?: 50f, priceReturn.toFloatOrNull() ?: 90f)
                }
            }) { Text(stringResource(R.string.create)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}