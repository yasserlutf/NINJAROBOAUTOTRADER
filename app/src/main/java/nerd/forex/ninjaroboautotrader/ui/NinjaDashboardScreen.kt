package nerd.forex.ninjaroboautotrader.ui

import androidx.compose.runtime.Composable

@Composable
fun NinjaDashboardScreen(viewModel: NinjaViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    NinjaTerminalScreen(viewModel = viewModel)
}
