package mk.fikt.mktransit.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import mk.fikt.mktransit.R
import mk.fikt.mktransit.ui.theme.*
import mk.fikt.mktransit.viewmodel.AuthState
import mk.fikt.mktransit.viewmodel.AuthViewModel

@Composable
fun WelcomeScreen(
    onEmailClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit,
    onAnonymousClick: () -> Unit,
    onLoginSuccess: (String) -> Unit = {},
    isTablet: Boolean = false,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    viewModel.loginWithGoogle(token)
                }
            } catch (e: ApiException) {
                android.util.Log.e("GoogleLogin", "Error: ${e.message}")
            }
        }
    }

    val callbackManager = remember {
        com.facebook.CallbackManager.Factory.create()
    }

    val facebookCallback = remember {
        object : com.facebook.FacebookCallback<com.facebook.login.LoginResult> {
            override fun onSuccess(result: com.facebook.login.LoginResult) {
                viewModel.loginWithFacebook(result.accessToken.token)
            }
            override fun onCancel() {}
            override fun onError(error: com.facebook.FacebookException) {
                android.util.Log.e("Facebook", "Error: ${error.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        com.facebook.login.LoginManager.getInstance()
            .registerCallback(callbackManager, facebookCallback)
    }

    var loginStarted by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (loginStarted && authState is AuthState.Success) {
            onLoginSuccess((authState as AuthState.Success).user.role.name)
        }
        if (loginStarted && authState is AuthState.NeedsRoleSelection) {
            onLoginSuccess("PASSENGER")
        }
    }

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
                    modifier = Modifier.fillMaxWidth().height(if (isTablet) 60.dp else 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextOnPrimary, contentColor = PrimaryBlue)
                ) {
                    Text(text = stringResource(R.string.btn_login_email), fontWeight = FontWeight.SemiBold, fontSize = if (isTablet) 18.sp else 16.sp)
                }

                OutlinedButton(
                    onClick = {
                        loginStarted = true
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val client = GoogleSignIn.getClient(context, gso)
                        googleSignInLauncher.launch(client.signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth().height(if (isTablet) 60.dp else 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextOnPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TextOnPrimary.copy(alpha = 0.7f))
                ) {
                    Text(text = stringResource(R.string.btn_login_google), fontWeight = FontWeight.SemiBold, fontSize = if (isTablet) 18.sp else 16.sp)
                }

                OutlinedButton(
                    onClick = {
                        loginStarted = true
                        com.facebook.login.LoginManager.getInstance()
                            .logInWithReadPermissions(
                                context as androidx.activity.result.ActivityResultRegistryOwner,
                                callbackManager,
                                listOf("public_profile")
                            )
                    },
                    modifier = Modifier.fillMaxWidth().height(if (isTablet) 60.dp else 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextOnPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TextOnPrimary.copy(alpha = 0.7f))
                ) {
                    Text(text = stringResource(R.string.btn_login_facebook), fontWeight = FontWeight.SemiBold, fontSize = if (isTablet) 18.sp else 16.sp)
                }

                TextButton(onClick = onAnonymousClick, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.btn_login_anonymous), color = TextOnPrimary.copy(alpha = 0.75f), fontSize = if (isTablet) 17.sp else 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}