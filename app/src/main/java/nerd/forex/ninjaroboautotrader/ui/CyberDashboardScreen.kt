package nerd.forex.ninjaroboautotrader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import nerd.forex.ninjaroboautotrader.data.model.BotSignal
import java.util.Locale

// Cyberpunk Theme Color Palette
val CyberDark = Color(0xFF0A0E17)
val CyberSurface = Color(0xFF131B2E)
val CyberCardBg = Color(0xFF1A233A)
val NeonGreen = Color(0xFF00FF66)
val NeonCyan = Color(0xFF00E5FF)
val NeonRed = Color(0xFFFF3366)
val TextMuted = Color(0xFF8B9BB4)

@Composable
fun CyberDashboardScreen(
    viewModel: TradingViewModel = viewModel()
) {
    // Collect live data streams from the ViewModel
    val accountState by viewModel.accountState.collectAsState()
    val signals by viewModel.botSignals.collectAsState()

    // Check if any bot is enabled for global status banner
    val isGlobalActive = signals.any { it.isEnabled }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CyberDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ==========================================
            // 1. HEADER & LIVE ACCOUNT TELEMETRY CARD
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NINJA ROBO // TERMINAL",
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isGlobalActive) "SYSTEM: LIVE [MT5 SYNCED]" else "SYSTEM: PAUSED",
                                color = if (isGlobalActive) NeonGreen else NeonRed,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }

                        // Master Kill Switch (Toggles all symbols)
                        Switch(
                            checked = isGlobalActive,
                            onCheckedChange = { newState ->
                                signals.forEach { signal ->
                                    viewModel.updateBotStatus(signal.symbol, newState)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberDark,
                                checkedTrackColor = NeonGreen,
                                uncheckedThumbColor = CyberDark,
                                uncheckedTrackColor = NeonRed
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ACCOUNT EQUITY",
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", accountState.equity)}",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Balance: $${String.format(Locale.US, "%,.2f", accountState.balance)}",
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Margin: $${String.format(Locale.US, "%,.2f", accountState.margin)}",
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Leverage: 1:${accountState.leverage}",
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TARGET ASSET BASKET (${signals.size} SYMBOLS)",
                color = TextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // 2. LIVE ASSET TELEMETRY LIST
            // ==========================================
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(signals) { signal ->
                    AdvancedBotCard(
                        signal = signal,
                        onSymbolToggle = { symbol, newState ->
                            viewModel.updateBotStatus(symbol, newState)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdvancedBotCard(
    signal: BotSignal,
    onSymbolToggle: (String, Boolean) -> Unit
) {
    val isHighSniper = signal.aiSniperScore >= 85
    val borderColor = if (isHighSniper) NeonGreen.copy(alpha = 0.6f) else CyberCardBg

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = signal.symbol,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (signal.trend == "BULLISH") NeonGreen.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = signal.trend,
                            color = if (signal.trend == "BULLISH") NeonGreen else NeonRed,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Switch(
                    checked = signal.isEnabled,
                    onCheckedChange = { newValue -> onSymbolToggle(signal.symbol, newValue) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberDark,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = CyberDark,
                        uncheckedTrackColor = TextMuted
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Sniper Score: ${signal.aiSniperScore}/100",
                    color = if (isHighSniper) NeonGreen else Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Spread: ${signal.spread}p",
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { signal.aiSniperScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isHighSniper) NeonGreen else NeonCyan,
                trackColor = CyberSurface,
            )
        }
    }
}
