package nerd.forex.ninjaroboautotrader.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import nerd.forex.ninjaroboautotrader.data.model.BotSignal
import nerd.forex.ninjaroboautotrader.data.model.GoldCooldownState

/**
 * Real-time Firebase Firestore and Realtime Database repository for NINJA ROBO AUTO TRADER.
 */
class NinjaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val userId: String = "default_user_id",
) {
    companion object {
        val TARGET_SYMBOLS = listOf("OIL", "XAUUSD", "EURUSD", "GBPUSD", "AUDCAD", "BTC")
        const val REALTIME_DB_URL = "https://astra-forex-myapplicatio-bd94d-default-rtdb.firebaseio.com"
    }

    fun getAssetStatuses(): Flow<List<BotSignal>> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("bot_status")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val assets = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(BotSignal::class.java)?.apply {
                            if (symbol.isEmpty()) {
                                symbol = doc.id
                            }
                        }
                    }
                    trySend(assets)
                }
            }
        awaitClose { listener.remove() }
    }

    // Write back individual asset toggle state to Firestore
    fun updateAssetToggle(symbol: String, isEnabled: Boolean) {
        firestore.collection("users")
            .document(userId)
            .collection("bot_status")
            .document(symbol)
            .update("isEnabled", isEnabled)
    }

    // Master Kill Switch: Loop through all assets and turn them off/on via a batch write
    fun setMasterKillSwitch(isEnabled: Boolean, currentAssets: List<BotSignal>) {
        if (currentAssets.isEmpty()) return
        val batch = firestore.batch()
        currentAssets.forEach { asset ->
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("bot_status")
                .document(asset.symbol)
            batch.update(docRef, "isEnabled", isEnabled)
        }
        batch.commit()
    }

    /**
     * Listens to real-time Gold Cooldown updates from Firebase Realtime Database.
     */
    fun listenToGoldCooldown(): Flow<GoldCooldownState> = callbackFlow {
        val databaseRef = FirebaseDatabase.getInstance(REALTIME_DB_URL)
            .getReference("gold_cooldown")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val remaining = snapshot.child("remaining").getValue(Long::class.java) ?: 0L
                val total = snapshot.child("total").getValue(Long::class.java) ?: 120L

                val minutes = remaining / 60
                val seconds = remaining % 60
                val formattedTime = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
                val progress = if (total > 0) (remaining.toFloat() / total.toFloat()) else 0f

                trySend(
                    GoldCooldownState(
                        remaining = remaining,
                        total = total,
                        formattedTime = formattedTime,
                        progress = progress
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        databaseRef.addValueEventListener(eventListener)
        awaitClose { databaseRef.removeEventListener(eventListener) }
    }
}
