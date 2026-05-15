package mk.fikt.mktransit.ui.screens.home

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import mk.fikt.mktransit.R
import mk.fikt.mktransit.viewmodel.MapViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val mapStops by viewModel.mapStops.collectAsStateWithLifecycle()

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            viewModel.setPermissionGranted(true)
            viewModel.fetchUserLocation(context)
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
        } else {
            viewModel.fetchUserLocation(context)
        }
        viewModel.loadStopsFromFirestore()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_map)) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        setBuiltInZoomControls(true)
                        controller.setZoom(13.0)

                        // Фиксно центрирај на Скопје — никогаш не поместувај
                        val skopje = GeoPoint(41.9981, 21.4254)
                        controller.setCenter(skopje)

                        // Прикажи локација но НЕ следи ја
                        val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                        myLocationOverlay.enableMyLocation()
                        myLocationOverlay.disableFollowLocation() // ← ВАЖНО
                        overlays.add(myLocationOverlay)
                    }
                },
                update = { mapView ->
                    // Само ажурирај маркери — не центрирај повторно
                    mapView.overlays.removeAll { it is Marker }

                    if (mapStops.isNotEmpty()) {
                        mapStops.forEach { stop ->
                            val marker = Marker(mapView)
                            marker.position = GeoPoint(stop.latitude, stop.longitude)
                            marker.title = stop.stopName
                            marker.snippet = "${stop.lineNumber} - ${stop.lineName}"
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            mapView.overlays.add(marker)
                        }
                    } else {
                        // Fallback — фиксни стопови во Скопје
                        val fallbackStops = listOf(
                            Triple("Центар", 41.9981, 21.4254),
                            Triple("Аеродром", 41.9700, 21.4800),
                            Triple("Железничка", 41.9964, 21.4314),
                            Triple("Кисела Вода", 41.9650, 21.4600),
                            Triple("Ново Лисиче", 42.0050, 21.4700)
                        )
                        fallbackStops.forEach { (name, lat, lon) ->
                            val marker = Marker(mapView)
                            marker.position = GeoPoint(lat, lon)
                            marker.title = name
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            mapView.overlays.add(marker)
                        }
                    }
                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            // Info card
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (userLocation != null) stringResource(R.string.your_location)
                            else stringResource(R.string.skopje_macedonia),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${mapStops.size} ${stringResource(R.string.stops)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Permission card
            if (!locationPermission.status.isGranted) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LocationOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.location_permission_needed),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                        TextButton(onClick = { locationPermission.launchPermissionRequest() }) {
                            Text(stringResource(R.string.enable))
                        }
                    }
                }
            }
        }
    }
}