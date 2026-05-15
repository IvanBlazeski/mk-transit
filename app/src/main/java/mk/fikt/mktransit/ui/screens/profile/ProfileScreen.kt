package mk.fikt.mktransit.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.fikt.mktransit.R
import mk.fikt.mktransit.domain.model.UserRole
import mk.fikt.mktransit.viewmodel.AuthState
import mk.fikt.mktransit.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onOperatorClick: () -> Unit,
    onFavoritesClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var hasLoggedOut by remember { mutableStateOf(false) }

    val isOperator = authState is AuthState.Success &&
            ((authState as AuthState.Success).user.role == UserRole.OPERATOR ||
                    (authState as AuthState.Success).user.role == UserRole.BOTH)

    LaunchedEffect(authState) {
        if (authState is AuthState.Idle && hasLoggedOut) onLogout()
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout)) },
            text = { Text(stringResource(R.string.logout_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    hasLoggedOut = true
                    viewModel.logout(context)
                }) {
                    Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile)) },
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    when (val state = authState) {
                        is AuthState.Success -> {
                            Text(
                                text = state.user.displayName.ifBlank { "User" },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = state.user.email,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = when (state.user.role) {
                                        UserRole.PASSENGER -> stringResource(R.string.passenger)
                                        UserRole.OPERATOR -> stringResource(R.string.operator)
                                        UserRole.BOTH -> stringResource(R.string.passenger_and_operator)
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = stringResource(R.string.guest),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Filled.Favorite,
                        title = stringResource(R.string.favorites),
                        subtitle = stringResource(R.string.favorites_subtitle),
                        onClick = onFavoritesClick
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(
                        icon = Icons.Filled.Notifications,
                        title = stringResource(R.string.notifications),
                        subtitle = stringResource(R.string.notifications_subtitle),
                        onClick = onNotificationsClick
                    )

                    // Само за оператори
                    if (isOperator) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileMenuItem(
                            icon = Icons.Filled.Business,
                            title = stringResource(R.string.operator_dashboard),
                            subtitle = stringResource(R.string.operator_dashboard_subtitle),
                            onClick = onOperatorClick
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(
                        icon = Icons.Filled.Language,
                        title = stringResource(R.string.language),
                        subtitle = stringResource(R.string.language_subtitle),
                        onClick = {}
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(
                        icon = Icons.Filled.Info,
                        title = stringResource(R.string.about),
                        subtitle = stringResource(R.string.about_subtitle),
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                ProfileMenuItem(
                    icon = Icons.Filled.Logout,
                    title = stringResource(R.string.logout),
                    subtitle = stringResource(R.string.logout_subtitle),
                    onClick = { showLogoutDialog = true },
                    isDestructive = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(text = subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}