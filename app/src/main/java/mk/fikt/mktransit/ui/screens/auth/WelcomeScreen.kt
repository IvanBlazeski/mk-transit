package mk.fikt.mktransit.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mk.fikt.mktransit.R
import mk.fikt.mktransit.ui.theme.*

@Composable
fun WelcomeScreen(
    onEmailClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit,
    onAnonymousClick: () -> Unit,
    isTablet: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryDarkBlue, PrimaryBlue, PrimaryLightBlue)
                )
            )
    ) {
        val contentModifier = if (isTablet) {
            Modifier
                .fillMaxHeight()
                .width(480.dp)
                .align(Alignment.Center)
                .padding(32.dp)
        } else {
            Modifier
                .fillMaxSize()
                .padding(32.dp)
        }

        Column(
            modifier = contentModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.DirectionsBus,
                    contentDescription = "MK Transit Logo",
                    tint = TextOnPrimary,
                    modifier = Modifier.size(if (isTablet) 100.dp else 80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.welcome_title),
                    fontSize = if (isTablet) 48.sp else 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.welcome_subtitle),
                    fontSize = if (isTablet) 20.sp else 16.sp,
                    color = TextOnPrimary.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEmailClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTablet) 60.dp else 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextOnPrimary,
                        contentColor = PrimaryBlue
                    )
                ) {
                    Text(
                        text = stringResource(R.string.btn_login_email),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (isTablet) 18.sp else 16.sp
                    )
                }

                OutlinedButton(
                    onClick = onGoogleClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTablet) 60.dp else 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextOnPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp, TextOnPrimary.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.btn_login_google),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (isTablet) 18.sp else 16.sp
                    )
                }

                OutlinedButton(
                    onClick = onFacebookClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTablet) 60.dp else 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextOnPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp, TextOnPrimary.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.btn_login_facebook),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (isTablet) 18.sp else 16.sp
                    )
                }

                TextButton(
                    onClick = onAnonymousClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.btn_login_anonymous),
                        color = TextOnPrimary.copy(alpha = 0.75f),
                        fontSize = if (isTablet) 17.sp else 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}