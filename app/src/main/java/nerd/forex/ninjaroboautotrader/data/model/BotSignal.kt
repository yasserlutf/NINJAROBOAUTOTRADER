package nerd.forex.ninjaroboautotrader.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * Data model representing real-time telemetry and per-pair risk settings for assets
 * monitored by the NINJA ROBO AUTO TRADER MT5 Bridge.
 */
@IgnoreExtraProperties
data class BotSignal(
    @get:PropertyName("symbol")
    @set:PropertyName("symbol")
    var symbol: String = "",

    @get:PropertyName("aiSniperScore")
    @set:PropertyName("aiSniperScore")
    var aiSniperScore: Int = 50,

    @get:PropertyName("spread")
    @set:PropertyName("spread")
    var spread: Double = 1.0,

    @get:PropertyName("rsi")
    @set:PropertyName("rsi")
    var rsi: Double = 50.0,

    @get:PropertyName("trend")
    @set:PropertyName("trend")
    var trend: String = "NEUTRAL",

    @get:PropertyName("isEnabled")
    @set:PropertyName("isEnabled")
    var isEnabled: Boolean = true,

    @get:PropertyName("lotSize")
    @set:PropertyName("lotSize")
    var lotSize: Double = 0.01,

    @get:PropertyName("slPoints")
    @set:PropertyName("slPoints")
    var slPoints: Int = 300,

    @get:PropertyName("tpPoints")
    @set:PropertyName("tpPoints")
    var tpPoints: Int = 600,

    @get:PropertyName("maxTrades")
    @set:PropertyName("maxTrades")
    var maxTrades: Int = 1
)
