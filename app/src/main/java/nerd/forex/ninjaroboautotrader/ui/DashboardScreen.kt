package nerd.forex.ninjaroboautotrader.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import nerd.forex.ninjaroboautotrader.R
import java.util.Calendar
import java.util.Locale

// Ninja Robo Cyber-Quant Palette
private val DashboardBg = Color(0xFF080E18)
private val DashboardSurface = Color(0xFF121D2D)
private val DashboardLine = Color(0xFF2B3B54)
private val DashboardText = Color(0xFFEFF4FC)
private val DashboardMuted = Color(0xFF91A0B5)
private val DashboardTeal = Color(0xFF63E1C3)
private val DashboardGreen = Color(0xFF59DCAA)
private val DashboardAmber = Color(0xFFFFC981)
private val DashboardRaised = Color(0xFF17243A)

data class DashboardUiState(
    val userName: String = "Yasser",
    val sessionName: String = "London session active",
    val brokerName: String = "Broker",
    val brokerConnected: Boolean = true,
    val equity: String = "$9,575.70",
    val dailyChange: String = "+0.34% TODAY",
    val balance: String = "$9,572.37",
    val usedMargin: String = "$61.81",
    val leverage: String = "1:500",
    val floatingPnl: String = "+$9.20",
    val floatingPnlPercent: String = "+0.10%",
    val openTrades: Int = 2,
    val winsToday: Int = 1,
    val lossesToday: Int = 1,
    val openLots: String = "0.03",
    val drawdown: String = "0.42%",
    val drawdownLimit: String = "3%",
    val openRisk: String = "$15.60",
    val freeMargin: String = "$9,513",
    val freeMarginPercent: String = "99.4% available",
    val exposure: String = "0.34%",
    val lossUsedToday: String = "$18.40",
    val dailyLossLimit: String = "$95.75",
    val lossLimitUsedPercent: Float = 0.19f,
    val lossLimitReset: String = "Resets in 5h 24m",
    val dailyLossStopEnabled: Boolean = true,
    val dailyLossStopAt: String = "−1.00%",
    val maxOpenPositions: Int = 4,
    val spreadProtectionEnabled: Boolean = true,
    val activePairs: Int = 10,
    val totalPairs: Int = 11,
    val lastScan: String = "9:40",
    val botRunning: Boolean = true
)

enum class DashboardDestination { HOME, TRADES, MARKETS, RISK, SETTINGS }

@Composable
fun NinjaRoboBrandLockup(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.download),
            contentDescription = "Ninja Robo Forex Logo",
            modifier = Modifier.size(if (compact) 32.dp else 44.dp)
        )
        Column {
            Text(
                text = "NINJA ROBO FOREX",
                color = DashboardTeal,
                fontSize = if (compact) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
            if (!compact) {
                Text(
                    text = "Institutional Quant Core",
                    color = DashboardMuted,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun LiveDashboardRoute(
    viewModel: TradingViewModel = viewModel(),
    selectedDestination: DashboardDestination = DashboardDestination.HOME,
    onDestinationSelected: (DashboardDestination) -> Unit = {}
) {
    val accountState by viewModel.accountState.collectAsState()
    val signals by viewModel.botSignals.collectAsState()

    val activePairsCount = signals.count { it.isEnabled }
    val isRunning = activePairsCount > 0

    val uiState = DashboardUiState(
        userName = "Lutfi",
        brokerName = "SCFM Limited",
        brokerConnected = true,
        equity = "$${String.format(Locale.US, "%,.2f", accountState.equity)}",
        balance = "$${String.format(Locale.US, "%,.2f", accountState.balance)}",
        usedMargin = "$${String.format(Locale.US, "%,.2f", accountState.margin)}",
        leverage = "1:${accountState.leverage}",
        activePairs = activePairsCount,
        totalPairs = signals.size,
        botRunning = isRunning
    )

    DashboardScreen(
        state = uiState,
        selectedDestination = selectedDestination,
        onPauseAutomation = {
            val newState = !isRunning
            signals.forEach { viewModel.updateBotStatus(it.symbol, newState) }
        },
        onDestinationSelected = onDestinationSelected
    )
}

@Composable
fun DashboardScreen(
    state: DashboardUiState = DashboardUiState(),
    selectedDestination: DashboardDestination = DashboardDestination.HOME,
    onPauseAutomation: () -> Unit = {},
    onDestinationSelected: (DashboardDestination) -> Unit = {}
) {
    Scaffold(
        containerColor = DashboardBg,
        bottomBar = {
            DashboardNavigationBar(
                selected = selectedDestination,
                onSelect = onDestinationSelected
            )
        }
    ) { scaffoldPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(DashboardBg)
                .padding(scaffoldPadding)
        ) {
            val isTablet = maxWidth >= 720.dp
            val sidePadding = if (isTablet) 24.dp else 16.dp

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = sidePadding,
                    end = sidePadding,
                    top = 16.dp,
                    bottom = 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    DashboardHeader(
                        state = state,
                        onBrokerSettingsClick = { onDestinationSelected(DashboardDestination.SETTINGS) }
                    )
                }

                if (isTablet) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AccountCard(state, Modifier.weight(1.15f))
                            PnlCard(state, Modifier.weight(0.85f))
                        }
                    }
                } else {
                    item { AccountCard(state) }
                    item { PnlCard(state) }
                }

                item { SectionHeading("RISK METRICS // TELEMETRY", "FULL AUDIT REPORT →") }
                item { RiskOverview(state, isTablet) }

                if (isTablet) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DailyLossCard(state, Modifier.weight(1f))
                            ProtectionRulesCard(state, Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BotStatusCard(state, Modifier.weight(1f))
                            PauseAutomationButton(
                                state = state,
                                onPause = onPauseAutomation,
                                modifier = Modifier.weight(0.72f)
                            )
                        }
                    }
                } else {
                    item { DailyLossCard(state) }
                    item { ProtectionRulesCard(state) }
                    item { BotStatusCard(state) }
                    item { PauseAutomationButton(state, onPauseAutomation) }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    state: DashboardUiState,
    onBrokerSettingsClick: () -> Unit = {}
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 3.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NinjaRoboBrandLockup(modifier = Modifier.weight(1f), compact = true)
            Surface(
                modifier = Modifier.clickable { onBrokerSettingsClick() },
                color = if (state.brokerConnected) Color(0xFF15322E) else Color(0xFF3A2229),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, if (state.brokerConnected) Color(0xFF2D5A50) else Color(0xFF62404A))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("●", color = if (state.brokerConnected) DashboardGreen else Color(0xFFFF8795), fontSize = 8.sp)
                    Text(
                        if (state.brokerConnected) "${state.brokerName.uppercase()} LIVE" else "DISCONNECTED",
                        color = if (state.brokerConnected) DashboardTeal else Color(0xFFFF8795),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Column {
            Text(
                text = "$greeting, ${state.userName}",
                color = DashboardText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(state.sessionName, color = DashboardMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun AccountCard(state: DashboardUiState, modifier: Modifier = Modifier) {
    DashboardCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Label("NET ACCOUNT EQUITY", Modifier.weight(1f))
            Pill(state.dailyChange, DashboardGreen)
        }
        Text(
            state.equity,
            color = DashboardText,
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1.5).sp,
            modifier = Modifier.padding(top = 5.dp)
        )
        HorizontalDivider(color = DashboardLine, modifier = Modifier.padding(vertical = 10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCell("BALANCE", state.balance, Modifier.weight(1f))
            StatCell("USED MARGIN", state.usedMargin, Modifier.weight(1f))
            StatCell("LEVERAGE", state.leverage, Modifier.weight(0.8f))
        }
    }
}

@Composable
private fun PnlCard(state: DashboardUiState, modifier: Modifier = Modifier) {
    DashboardCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Label("FLOATING OPEN P&L")
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(state.floatingPnl, color = DashboardGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(state.floatingPnlPercent, color = DashboardGreen, fontSize = 10.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
            Pill("${state.openTrades} ACTIVE POSITIONS", DashboardGreen)
        }
        HorizontalDivider(color = DashboardLine, modifier = Modifier.padding(vertical = 10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StatCell("WINS TODAY", state.winsToday.toString(), Modifier.weight(1f), DashboardGreen)
            StatCell("LOSSES", state.lossesToday.toString(), Modifier.weight(1f))
            StatCell("OPEN LOTS", state.openLots, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RiskOverview(state: DashboardUiState, isTablet: Boolean) {
    val metrics = listOf(
        Triple("DRAWDOWN", state.drawdown, "Within ${state.drawdownLimit} threshold"),
        Triple("OPEN EXPOSURE RISK", state.openRisk, "Across ${state.openTrades} positions"),
        Triple("FREE LIQUIDITY", state.freeMargin, state.freeMarginPercent),
        Triple("TOTAL BASKET EXPOSURE", state.exposure, "of account equity")
    )
    if (isTablet) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            metrics.forEachIndexed { index, metric ->
                MetricCard(metric.first, metric.second, metric.third, Modifier.weight(1f), if (index == 0) DashboardGreen else DashboardText)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                MetricCard(metrics[0].first, metrics[0].second, metrics[0].third, Modifier.weight(1f), DashboardGreen)
                MetricCard(metrics[1].first, metrics[1].second, metrics[1].third, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                MetricCard(metrics[2].first, metrics[2].second, metrics[2].third, Modifier.weight(1f))
                MetricCard(metrics[3].first, metrics[3].second, metrics[3].third, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DailyLossCard(state: DashboardUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        SectionHeading("DAILY DRAWDOWN SHIELD", "OPTIMAL RISK", trailingColor = DashboardGreen)
        DashboardCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Label("DRAWDOWN CONSUMED TODAY")
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(state.lossUsedToday, color = DashboardText, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text("of limit ${state.dailyLossLimit}", color = DashboardMuted, fontSize = 9.sp, modifier = Modifier.padding(bottom = 3.dp))
                    }
                }
                Text("${(state.lossLimitUsedPercent * 100).toInt()}% USED", color = DashboardAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { state.lossLimitUsedPercent.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(5.dp),
                color = DashboardAmber,
                trackColor = Color(0xFF2A3546)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(state.lossLimitReset, color = DashboardMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text("Max cap ${state.dailyLossLimit}", color = DashboardMuted, fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun ProtectionRulesCard(state: DashboardUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        SectionHeading("AUTOMATED SAFETY PROTOCOLS", "CONFIGURE →")
        DashboardCard {
            ProtectionRule("Daily loss circuit breaker", "Halts bot at ${state.dailyLossStopAt}", state.dailyLossStopEnabled)
            HorizontalDivider(color = DashboardLine)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Max open positions", color = DashboardText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Global basket limitation", color = DashboardMuted, fontSize = 9.sp)
                }
                Text(state.maxOpenPositions.toString(), color = DashboardText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = DashboardLine)
            ProtectionRule("Spread filter guard", "Bypasses high-cost market entries", state.spreadProtectionEnabled)
        }
    }
}

@Composable
private fun ProtectionRule(title: String, description: String, enabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = DashboardText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(description, color = DashboardMuted, fontSize = 9.sp)
        }
        Pill(if (enabled) "ARMED" else "DISARMED", if (enabled) DashboardGreen else DashboardMuted)
    }
}

@Composable
private fun BotStatusCard(state: DashboardUiState, modifier: Modifier = Modifier) {
    DashboardCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color(0xFF173B36), shape = RoundedCornerShape(10.dp)) {
                Text("◈", color = DashboardTeal, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 9.dp)
            ) {
                Text(
                    "Quant Engine · ${if (state.botRunning) "Active Streaming" else "Paused"}",
                    color = DashboardText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${state.activePairs} of ${state.totalPairs} pairs online · Last poll ${state.lastScan}",
                    color = DashboardMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Pill(if (state.botRunning) "LIVE" else "PAUSED", if (state.botRunning) DashboardGreen else DashboardAmber)
        }
    }
}

@Composable
private fun PauseAutomationButton(
    state: DashboardUiState,
    onPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Button(
            onClick = onPause,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 46.dp),
            shape = RoundedCornerShape(11.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DashboardAmber,
                contentColor = Color(0xFF1F1A12)
            )
        ) {
            Text(if (state.botRunning) "EMERGENCY HALT SYSTEM" else "RESUME QUANT ENGINE", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
        }
        Text(
            "Halts new entries instantly. Existing trades remain actively managed.",
            color = DashboardMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
    valueColor: Color = DashboardText
) {
    Surface(
        modifier = modifier,
        color = DashboardRaised,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF273850))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Label(title)
            Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
            Text(detail, color = DashboardMuted, fontSize = 8.sp, maxLines = 1, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    trailing: String,
    trailingColor: Color = DashboardTeal
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp, vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = DashboardText, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text(trailing, color = trailingColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DashboardCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardSurface),
        border = BorderStroke(1.dp, DashboardLine)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = content
        )
    }
}

@Composable
private fun Label(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        color = DashboardMuted,
        fontSize = 7.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        maxLines = 1
    )
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = DashboardText) {
    Column(modifier = modifier) {
        Label(label)
        Text(value, color = valueColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp), maxLines = 1)
    }
}

@Composable
private fun Pill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.13f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Text(text, color = color, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), maxLines = 1)
    }
}

@Composable
private fun DashboardNavigationBar(
    selected: DashboardDestination,
    onSelect: (DashboardDestination) -> Unit
) {
    val items = listOf(
        Triple(DashboardDestination.HOME, "⌂", "Home"),
        Triple(DashboardDestination.TRADES, "▤", "Trades"),
        Triple(DashboardDestination.MARKETS, "⌁", "Markets"),
        Triple(DashboardDestination.RISK, "◈", "Risk"),
        Triple(DashboardDestination.SETTINGS, "☷", "Settings")
    )
    NavigationBar(
        containerColor = Color(0xFF111C2E),
        contentColor = DashboardMuted,
        tonalElevation = 0.dp
    ) {
        items.forEach { (destination, glyph, label) ->
            NavigationBarItem(
                selected = selected == destination,
                onClick = { onSelect(destination) },
                icon = { Text(glyph, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                label = { Text(label, fontSize = 8.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DashboardTeal,
                    selectedTextColor = DashboardTeal,
                    indicatorColor = Color(0xFF1B2B3E),
                    unselectedIconColor = DashboardMuted,
                    unselectedTextColor = DashboardMuted
                )
            )
        }
    }
}
