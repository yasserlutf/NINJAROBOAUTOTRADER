package nerd.forex.ninjaroboautotrader.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import nerd.forex.ninjaroboautotrader.data.model.AccountState
import nerd.forex.ninjaroboautotrader.data.model.BotSignal

class TradingViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // Dynamic Multi-tenant UID from Firebase Auth
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user_id"

    private val _accountState = MutableStateFlow(AccountState())
    val accountState: StateFlow<AccountState> = _accountState

    private val _botSignals = MutableStateFlow<List<BotSignal>>(emptyList())
    val botSignals: StateFlow<List<BotSignal>> = _botSignals

    init {
        startListeningToAccount()
        startListeningToSignals()
    }

    fun startListeningToAccount() {
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TradingViewModel", "Account listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val balance = snapshot.getDouble("balance") ?: 0.0
                    val equity = snapshot.getDouble("equity") ?: 0.0
                    val margin = snapshot.getDouble("margin") ?: 0.0
                    val leverage = snapshot.getLong("leverage")?.toInt() ?: 1

                    _accountState.value = AccountState(balance, equity, margin, leverage)
                }
            }
    }

    fun startListeningToSignals() {
        db.collection("users")
            .document(userId)
            .collection("bot_status")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TradingViewModel", "Signals listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val signals = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(BotSignal::class.java)?.apply {
                            if (symbol.isEmpty()) {
                                symbol = doc.id
                            }
                        }
                    }
                    _botSignals.value = signals
                }
            }
    }

    fun updateBotStatus(symbol: String, isEnabled: Boolean) {
        db.collection("users")
            .document(userId)
            .collection("bot_status")
            .document(symbol)
            .update("isEnabled", isEnabled)
            .addOnFailureListener { e ->
                Log.e("TradingViewModel", "Failed to update bot status for $symbol", e)
            }
    }

    fun updateSymbolRisk(symbol: String, lotSize: Double, slPoints: Int, tpPoints: Int, maxTrades: Int) {
        db.collection("users")
            .document(userId)
            .collection("bot_status")
            .document(symbol)
            .update(
                mapOf(
                    "lotSize" to lotSize,
                    "slPoints" to slPoints,
                    "tpPoints" to tpPoints,
                    "maxTrades" to maxTrades
                )
            )
            .addOnFailureListener { e ->
                Log.e("TradingViewModel", "Failed to update risk settings for $symbol", e)
            }
    }
}
