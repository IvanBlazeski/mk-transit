package mk.fikt.mktransit.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.fikt.mktransit.R
import mk.fikt.mktransit.domain.model.UserRole
import mk.fikt.mktransit.viewmodel.AuthState
import mk.fikt.mktransit.viewmodel.AuthViewModel

@Composable
fun RoleSelectionScreen(
    onRoleSelected: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onRoleSelected()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.DirectionsBus,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.role_selection_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.role_selection_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        RoleCard(
            icon = Icons.Filled.Person,
            title = stringResource(R.string.role_passenger),
            description = stringResource(R.string.role_passenger_desc),
            isSelected = selectedRole == UserRole.PASSENGER,
            onClick = { selectedRole = UserRole.PASSENGER }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RoleCard(
            icon = Icons.Filled.Business,
            title = stringResource(R.string.role_operator),
            description = stringResource(R.string.role_operator_desc),
            isSelected = selectedRole == UserRole.OPERATOR,
            onClick = { selectedRole = UserRole.OPERATOR }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RoleCard(
            icon = Icons.Filled.DirectionsBus,
            title = stringResource(R.string.role_both),
            description = stringResource(R.string.role_both_desc),
            isSelected = selectedRole == UserRole.BOTH,
            onClick = { selectedRole = UserRole.BOTH }
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                selectedRole?.let { viewModel.setUserRole(it) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = selectedRole != null && authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    stringResource(R.string.btn_continue),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun RoleCard(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
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
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}