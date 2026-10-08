package nerd.forex.ninjaroboautotrader.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import nerd.forex.ninjaroboautotrader.data.model.BotSignal
import nerd.forex.ninjaroboautotrader.ui.theme.CardBg
import nerd.forex.ninjaroboautotrader.ui.theme.CardBorder
import nerd.forex.ninjaroboautotrader.ui.theme.DarkBg
import nerd.forex.ninjaroboautotrader.ui.theme.NeonCyan
import nerd.forex.ninjaroboautotrader.ui.theme.NeonGreen
import nerd.forex.ninjaroboautotrader.ui.theme.NeonPurple
import nerd.forex.ninjaroboautotrader.ui.theme.NeonRed
import nerd.forex.ninjaroboautotrader.ui.theme.TextLight
import nerd.forex.ninjaroboautotrader.ui.theme.TextMuted

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)

@Composable
fun NinjaTerminalScreen(viewModel: NinjaViewModel = viewModel()) {
    val assets by viewModel.assetStatuses.collectAsState()
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    // Check if any asset is currently active to determine Master Kill Switch visual state
    val isAnyActive = assets.any { it.isEnabled }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            NinjaBottomNavBar(selectedIndex = selectedNavIndex) { selectedNavIndex = it }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. HEADER SECTION & MASTER KILL SWITCH
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NINJA AUTO TRADER",
                            color = TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "QUANT EXECUTION ENGINE",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            letterSpacing = 1.5.sp
                        )
                    }

                    // Master Kill Switch Button
                    Button(
                        onClick = { viewModel.masterKillSwitch(!isAnyActive) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAnyActive) NeonRed.copy(alpha = 0.2f) else NeonGreen.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(1.dp, if (isAnyActive) NeonRed else NeonGreen),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isAnyActive) NeonRed else NeonGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAnyActive) "KILL SWITCH" else "SYSTEM OFF",
                            color = if (isAnyActive) NeonRed else NeonGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. COMMAND CENTER & METRICS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "ACCOUNT EQUITY",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$9,575.70",
                                    color = TextLight,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "BALANCE $9,572.37", color = TextMuted, fontSize = 10.sp)
                                Text(text = "MARGIN $61.81", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = "LEVERAGE 1:500",
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. MARKET INTELLIGENCE HIGHLIGHT
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = NeonPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MARKET INTELLIGENCE",
                                    color = TextLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(text = "AI ANALYSIS ACTIVE", color = NeonCyan, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MarketMetricBox("RSI (14)", "50.0", "NEUTRAL")
                            MarketMetricBox("EMA 20", "156.31", "TRENDING")
                            MarketMetricBox("SPREAD", "0.2p", "TIGHT")
                            MarketMetricBox("CONFIDENCE", "85%", "HIGH", highlightColor = NeonGreen)
                        }
                    }
                }
            }

            // 4. MULTI-ASSET BASKET HEADER
            item {
                Text(
                    text = "TARGET ASSET BASKET (6 SYMBOLS)",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Asset list items
            if (assets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Awaiting Firebase Telemetry...\nMake sure Firestore path 'users/default_user_id/bot_status' has documents for OIL, XAUUSD, EURUSD, GBPUSD, AUDCAD, BTC",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(assets) { asset ->
                    AssetSniperCard(asset = asset) {
                        viewModel.toggleAsset(asset.symbol, asset.isEnabled)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun MarketMetricBox(
    label: String,
    value: String,
    subtext: String,
    highlightColor: Color = NeonCyan
) {
    Column(
        modifier = Modifier
            .background(DarkBg, RoundedCornerShape(10.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = TextMuted, fontSize = 9.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = highlightColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(text = subtext, color = TextMuted, fontSize = 8.sp)
    }
}

@Composable
fun AssetSniperCard(asset: BotSignal, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, if (asset.isEnabled) NeonCyan.copy(alpha = 0.5f) else CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (asset.isEnabled) NeonCyan.copy(alpha = 0.15f) else TextMuted.copy(
                                alpha = 0.1f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (asset.isEnabled) NeonCyan else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = asset.symbol,
                        color = TextLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "AI Sniper Score: ${asset.aiSniperScore}/100 • Spread: ${asset.spread}p",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
            Switch(
                checked = asset.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkBg,
                    checkedTrackColor = NeonGreen,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DarkBg
                )
            )
        }
    }
}

@Composable
fun NinjaBottomNavBar(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = CardBg,
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            NavItem("Terminal", Icons.Default.Home, 0),
            NavItem("News Intel", Icons.Default.Info, 1),
            NavItem("Risk & Rules", Icons.Default.Settings, 2),
            NavItem("Journal", Icons.Default.List, 3)
        )
        navItems.forEach { navItem ->
            NavigationBarItem(
                icon = {
                    Icon(
                        navItem.icon,
                        contentDescription = navItem.label,
                        tint = if (selectedIndex == navItem.index) NeonCyan else TextMuted
                    )
                },
                label = {
                    Text(
                        navItem.label,
                        color = if (selectedIndex == navItem.index) NeonCyan else TextMuted,
                        fontSize = 10.sp
                    )
                },
                selected = selectedIndex == navItem.index,
                onClick = { onItemSelected(navItem.index) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = DarkBg)
            )
        }
    }
}
