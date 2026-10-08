package nerd.forex.ninjaroboautotrader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import nerd.forex.ninjaroboautotrader.data.model.BotSignal

@Composable
fun TradingDashboardScreen(viewModel: TradingViewModel = viewModel()) {
    val signals by viewModel.botSignals.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(signals) { signal ->
            BotSignalCard(signal = signal)
        }
    }
}

@Composable
fun BotSignalCard(signal: BotSignal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = signal.symbol, fontWeight = Bold, fontSize = 18.sp)
                Text(
                    text = "Score: ${signal.aiSniperScore}/100",
                    color = if (signal.aiSniperScore >= 85) Color.Green else Color.Gray,
                    fontWeight = Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Trend: ${signal.trend} | RSI: ${signal.rsi}")
            Text(text = "Spread: ${signal.spread}p")
        }
    }
}
