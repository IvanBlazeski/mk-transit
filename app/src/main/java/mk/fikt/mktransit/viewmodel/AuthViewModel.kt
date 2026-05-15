package mk.fikt.mktransit.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mk.fikt.mktransit.domain.model.User
import mk.fikt.mktransit.domain.model.UserRole
import javax.inject.Inject

// UI State — сè што екранот треба да знае
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    object NeedsRoleSelection : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Провери дали корисникот е веќе најавен
    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadUserFromFirestore(currentUser.uid)
        }
    }

    // ── EMAIL / PASSWORD LOGIN ────────────────────────────────────
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("No user ID")
                loadUserFromFirestore(uid)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    // ── EMAIL / PASSWORD REGISTER ─────────────────────────────────
    fun registerWithEmail(email: String, password: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("No user ID")
                // Нов корисник — треба да избере улога
                createUserInFirestore(uid, email, name)
                _authState.value = AuthState.NeedsRoleSelection
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    // ── GOOGLE SIGN-IN ────────────────────────────────────────────
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val uid = result.user?.uid ?: throw Exception("No user ID")
                val isNew = result.additionalUserInfo?.isNewUser ?: false
                if (isNew) {
                    createUserInFirestore(
                        uid = uid,
                        email = result.user?.email ?: "",
                        name = result.user?.displayName ?: ""
                    )
                    _authState.value = AuthState.NeedsRoleSelection
                } else {
                    loadUserFromFirestore(uid)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google login failed")
            }
        }
    }

    // ── ANONYMOUS LOGIN ───────────────────────────────────────────
    fun loginAnonymously() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInAnonymously().await()
                _authState.value = AuthState.Success(User(displayName = "Guest"))
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Anonymous login failed")
            }
        }
    }

    fun loginWithFacebook(token: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credential = com.google.firebase.auth.FacebookAuthProvider
                    .getCredential(token)
                val result = auth.signInWithCredential(credential).await()
                val uid = result.user?.uid ?: throw Exception("No user ID")
                val isNew = result.additionalUserInfo?.isNewUser ?: false
                if (isNew) {
                    createUserInFirestore(
                        uid = uid,
                        email = result.user?.email ?: "",
                        name = result.user?.displayName ?: ""
                    )
                    _authState.value = AuthState.NeedsRoleSelection
                } else {
                    loadUserFromFirestore(uid)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Facebook login failed")
            }
        }
    }

    // ── FORGOT PASSWORD ───────────────────────────────────────────
    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Reset failed")
            }
        }
    }

    // ── SET ROLE ──────────────────────────────────────────────────
    fun setUserRole(role: UserRole) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            try {
                firestore.collection("users").document(uid)
                    .update("role", role.name).await()
                loadUserFromFirestore(uid)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to set role")
            }
        }
    }

    // ── LOGOUT ────────────────────────────────────────────────────
    fun logout(context: android.content.Context) {
        auth.signOut()
        // Одјави се и од Google
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions
            .Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
            .build()
        com.google.android.gms.auth.api.signin.GoogleSignIn
            .getClient(context, gso)
            .signOut()
        _authState.value = AuthState.Idle
    }

    // ── HELPERS ───────────────────────────────────────────────────
    private suspend fun createUserInFirestore(uid: String, email: String, name: String) {
        val user = hashMapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to name,
            "role" to UserRole.PASSENGER.name,
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(uid).set(user).await()
    }

    private fun loadUserFromFirestore(uid: String) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val email = doc.getString("email") ?: ""
                    val operatorEmail = "ivan@operator.mk"

                    val role = if (email == operatorEmail) UserRole.OPERATOR else UserRole.PASSENGER

                    if (email == operatorEmail && doc.getString("role") != "OPERATOR") {
                        firestore.collection("users").document(uid)
                            .update("role", "OPERATOR").await()
                    }

                    val user = User(
                        uid = uid,
                        email = email,
                        displayName = doc.getString("displayName") ?: "",
                        role = role
                    )
                    _authState.value = AuthState.Success(user)
                } else {
                    _authState.value = AuthState.NeedsRoleSelection
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to load user")
            }
        }
    }

    // Google Sign-In Client
    fun getGoogleSignInClient(context: Context) = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("262264957123-3b0jio1olkb2678770n66rhqcflb2f1h.apps.googleusercontent.com")
            .requestEmail()
            .build()
    )
}