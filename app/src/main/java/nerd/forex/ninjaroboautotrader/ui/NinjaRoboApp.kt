package nerd.forex.ninjaroboautotrader.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NinjaRoboApp(viewModel: TradingViewModel = viewModel()) {
    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }

    if (currentUser == null) {
        LoginScreen(
            onGoogleSignInClicked = {
                // Trigger Google Sign-In Intent / Credential Manager here.
                // Upon successful Google authentication with Firebase:
                // currentUser = FirebaseAuth.getInstance().currentUser
            }
        )
    } else {
        // Render main dashboard root once logged in
        MainDashboardRoot(viewModel = viewModel)
    }
}
