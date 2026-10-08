package nerd.forex.ninjaroboautotrader

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import nerd.forex.ninjaroboautotrader.ui.CyberDark
import nerd.forex.ninjaroboautotrader.ui.DashboardDestination
import nerd.forex.ninjaroboautotrader.ui.LiveDashboardRoute
import nerd.forex.ninjaroboautotrader.ui.LoginScreen
import nerd.forex.ninjaroboautotrader.ui.MainDashboardRoot
import nerd.forex.ninjaroboautotrader.ui.TradingViewModel
import nerd.forex.ninjaroboautotrader.ui.theme.NINJAROBOAUTOTRADERTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val credentialManager = CredentialManager.create(this)

        setContent {
            NINJAROBOAUTOTRADERTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberDark
                ) {
                    val viewModel: TradingViewModel = viewModel()
                    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
                    var selectedDestination by remember { mutableStateOf(DashboardDestination.HOME) }
                    val coroutineScope = rememberCoroutineScope()

                    if (currentUser == null) {
                        LoginScreen(
                            onGoogleSignInClicked = {
                                coroutineScope.launch {
                                    try {
                                        val googleIdOption = GetGoogleIdOption.Builder()
                                            .setFilterByAuthorizedAccounts(false)
                                            .setServerClientId(getString(R.string.default_web_client_id))
                                            .setAutoSelectEnabled(false)
                                            .build()

                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()

                                        val result = credentialManager.getCredential(
                                            context = this@MainActivity,
                                            request = request
                                        )

                                        val credential = result.credential
                                        if (credential is GoogleIdTokenCredential) {
                                            val googleIdToken = credential.idToken
                                            val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                            FirebaseAuth.getInstance().signInWithCredential(authCredential)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        currentUser = FirebaseAuth.getInstance().currentUser
                                                        viewModel.startListeningToAccount()
                                                        viewModel.startListeningToSignals()
                                                    }
                                                }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("MainActivity", "Google Sign-In failed", e)
                                    }
                                }
                            }
                        )
                    } else {
                        when (selectedDestination) {
                            DashboardDestination.HOME -> LiveDashboardRoute(
                                viewModel = viewModel,
                                selectedDestination = selectedDestination,
                                onDestinationSelected = { destination ->
                                    selectedDestination = destination
                                }
                            )
                            DashboardDestination.RISK -> MainDashboardRoot(viewModel = viewModel)
                            else -> LiveDashboardRoute(
                                viewModel = viewModel,
                                selectedDestination = selectedDestination,
                                onDestinationSelected = { destination ->
                                    selectedDestination = destination
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
