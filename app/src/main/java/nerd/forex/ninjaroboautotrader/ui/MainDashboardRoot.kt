package nerd.forex.ninjaroboautotrader.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import nerd.forex.ninjaroboautotrader.data.model.BotSignal
import java.util.Locale

sealed class Screen(val route: String, val title: String) {
    object Terminal : Screen("terminal", "Terminal")
    object RiskManagement : Screen("risk", "Risk Management")
}

@Composable
fun MainDashboardRoot(viewModel: TradingViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf<Screen>(Screen.Terminal) }
    val signals by viewModel.botSignals.collectAsState()

    Scaffold(
        containerColor = CyberDark,
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Terminal") },
                    label = { Text("Terminal", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    selected = selectedTab == Screen.Terminal,
                    onClick = { selectedTab = Screen.Terminal },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan, selectedTextColor = NeonCyan,
                        unselectedIconColor = TextMuted, unselectedTextColor = TextMuted,
                        indicatorColor = CyberCardBg
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Security, contentDescription = "Risk Management") },
                    label = { Text("Risk Mgmt", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    selected = selectedTab == Screen.RiskManagement,
                    onClick = { selectedTab = Screen.RiskManagement },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan, selectedTextColor = NeonCyan,
                        unselectedIconColor = TextMuted, unselectedTextColor = TextMuted,
                        indicatorColor = CyberCardBg
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                is Screen.Terminal -> TerminalScreen(viewModel, signals)
                is Screen.RiskManagement -> RiskManagementScreen(viewModel)
            }
        }
    }
}

@Composable
fun TerminalScreen(viewModel: TradingViewModel, signals: List<BotSignal>) {
    val accountState by viewModel.accountState.collectAsState()
    val activeCount = signals.count { it.isEnabled }
    val isGlobalActive = activeCount > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NINJA ROBO // SYSTEM",
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isGlobalActive) "ACTIVE [$activeCount/${signals.size} PAIRS]" else "SYSTEM PAUSED",
                        color = if (isGlobalActive) NeonGreen else NeonRed,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }

                Switch(
                    checked = isGlobalActive,
                    onCheckedChange = { newState ->
                        signals.forEach { viewModel.updateBotStatus(it.symbol, newState) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberDark, checkedTrackColor = NeonGreen,
                        uncheckedThumbColor = CyberDark, uncheckedTrackColor = NeonRed
                    )
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("ACCOUNT EQUITY", color = TextMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format(Locale.US, "%,.2f", accountState.equity)}",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CyberCardBg, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AccountMetric("BALANCE", "$${String.format(Locale.US, "%,.2f", accountState.balance)}")
                    AccountMetric("MARGIN", "$${String.format(Locale.US, "%,.2f", accountState.margin)}")
                    AccountMetric("LEVERAGE", "1:${accountState.leverage}")
                }
            }
        }
    }
}

@Composable
fun AccountMetric(label: String, value: String) {
    Column {
        Text(text = label, color = TextMuted, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
fun RiskManagementScreen(viewModel: TradingViewModel) {
    val signals by viewModel.botSignals.collectAsState()
    val selectedCount = signals.count { it.isEnabled }

    // Shared default fields state
    var lotSize by remember { mutableStateOf("0.01") }
    var maxTrades by remember { mutableStateOf("1") }
    var stopLoss by remember { mutableStateOf("300") }
    var takeProfit by remember { mutableStateOf("600") }
    var maxSpread by remember { mutableStateOf("2.0") }
    var aiFilterEnabled by remember { mutableStateOf(true) }
    var minSignalScore by remember { mutableFloatStateOf(60f) }

    // Live USD Risk & Reward Valuations
    val lotVal = lotSize.toDoubleOrNull() ?: 0.01
    val slPtsVal = stopLoss.toIntOrNull() ?: 300
    val tpPtsVal = takeProfit.toIntOrNull() ?: 600
    val estimatedSlUsd = lotVal * slPtsVal * 1.0
    val estimatedTpUsd = lotVal * tpPtsVal * 1.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark)
    ) {
        // Screen Header
        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Risk settings",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = "Manage shared risk parameters and default trade rules across your basket.",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        // Unified Scrollable Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Portfolio Risk Card
            item {
                PortfolioRiskCard(selectedCount = selectedCount)
            }

            // 2. Default Trade Settings Card
            item {
                SettingsCard(
                    lotSize = lotSize, onLotSizeChange = { lotSize = it },
                    maxTrades = maxTrades, onMaxTradesChange = { maxTrades = it },
                    stopLoss = stopLoss, onStopLossChange = { stopLoss = it },
                    takeProfit = takeProfit, onTakeProfitChange = { takeProfit = it },
                    maxSpread = maxSpread, onMaxSpreadChange = { maxSpread = it },
                    aiFilterEnabled = aiFilterEnabled, onAiFilterChange = { aiFilterEnabled = it },
                    minSignalScore = minSignalScore, onMinSignalScoreChange = { minSignalScore = it },
                    estimatedSlUsd = estimatedSlUsd,
                    estimatedTpUsd = estimatedTpUsd,
                    onSave = {
                        signals.filter { it.isEnabled }.forEach { signal ->
                            val lot = lotSize.toDoubleOrNull() ?: 0.01
                            val sl = stopLoss.toIntOrNull() ?: 300
                            val tp = takeProfit.toIntOrNull() ?: 600
                            val max = maxTrades.toIntOrNull() ?: 1
                            viewModel.updateSymbolRisk(signal.symbol, lot, sl, tp, max)
                        }
                    }
                )
            }

            // Footer note
            item {
                Text(
                    text = "These defaults apply automatically to all enabled pairs in your strategy basket.",
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PortfolioRiskCard(selectedCount: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = BorderStroke(1.dp, CyberCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Portfolio risk", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("$selectedCount pairs enabled", color = TextMuted, fontSize = 10.sp)
                }
                Surface(color = NeonGreen.copy(alpha = 0.14f), shape = RoundedCornerShape(20.dp)) {
                    Text("WITHIN LIMIT", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp))
                }
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text("0.34%", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("  current exposure", color = TextMuted, fontSize = 10.sp, modifier = Modifier.padding(bottom = 3.dp))
                Spacer(Modifier.weight(1f))
                Text("Max 1.00%", color = TextMuted, fontSize = 10.sp, modifier = Modifier.padding(bottom = 3.dp))
            }
            LinearProgressIndicator(
                progress = { 0.34f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = NeonGreen,
                trackColor = CyberCardBg
            )
        }
    }
}

@Composable
private fun SettingsCard(
    lotSize: String, onLotSizeChange: (String) -> Unit,
    maxTrades: String, onMaxTradesChange: (String) -> Unit,
    stopLoss: String, onStopLossChange: (String) -> Unit,
    takeProfit: String, onTakeProfitChange: (String) -> Unit,
    maxSpread: String, onMaxSpreadChange: (String) -> Unit,
    aiFilterEnabled: Boolean, onAiFilterChange: (Boolean) -> Unit,
    minSignalScore: Float, onMinSignalScoreChange: (Float) -> Unit,
    estimatedSlUsd: Double, estimatedTpUsd: Double,
    onSave: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = BorderStroke(1.dp, CyberCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Column {
                Text("Default trade settings", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Shared by every enabled pair", color = TextMuted, fontSize = 10.sp)
            }
            Text("POSITION SIZING", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                NumberSettingField("Lot size", lotSize, onLotSizeChange, "lots", Modifier.weight(1f))
                NumberSettingField("Max trades", maxTrades, onMaxTradesChange, "per pair", Modifier.weight(1f))
            }

            Text("PROTECTION", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

            Column {
                NumberSettingField("Stop loss", stopLoss, onStopLossChange, "points", Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(2.dp))
                Text("≈ $${String.format(Locale.US, "%,.2f", estimatedSlUsd)} USD risk", color = NeonRed, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Column {
                NumberSettingField("Take profit", takeProfit, onTakeProfitChange, "points", Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(2.dp))
                Text("≈ $${String.format(Locale.US, "%,.2f", estimatedTpUsd)} USD profit", color = NeonGreen, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            NumberSettingField("Maximum spread", maxSpread, onMaxSpreadChange, "pips", Modifier.fillMaxWidth())

            HorizontalDivider(color = CyberCardBg)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("AI signal filter", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Ignore entries below your score threshold", color = TextMuted, fontSize = 10.sp)
                }
                Switch(
                    checked = aiFilterEnabled,
                    onCheckedChange = onAiFilterChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = CyberDark, checkedTrackColor = NeonCyan)
                )
            }
            if (aiFilterEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Minimum signal score", color = TextMuted, fontSize = 10.sp)
                        Text("${minSignalScore.toInt()} / 100", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = minSignalScore,
                        onValueChange = onMinSignalScoreChange,
                        valueRange = 50f..95f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan, inactiveTrackColor = CyberCardBg)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Strategy Settings", color = CyberDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun NumberSettingField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.isEmpty() || input.matches(Regex("^\\d*(\\.\\d*)?$"))) onValueChange(input)
        },
        modifier = modifier,
        label = { Text(label, fontSize = 10.sp) },
        trailingIcon = { Text(unit, color = TextMuted, fontSize = 9.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(10.dp),
        colors = textFieldColors()
    )
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = CyberCardBg,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = TextMuted,
    cursorColor = NeonCyan,
    focusedContainerColor = CyberDark,
    unfocusedContainerColor = CyberDark
)
