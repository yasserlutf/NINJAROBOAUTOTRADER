package nerd.forex.ninjaroboautotrader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nerd.forex.ninjaroboautotrader.data.model.BotSignal
import nerd.forex.ninjaroboautotrader.data.model.GoldCooldownState
import nerd.forex.ninjaroboautotrader.data.repository.NinjaRepository

class NinjaViewModel(
    private val repository: NinjaRepository = NinjaRepository()
) : ViewModel() {

    val assetStatuses: StateFlow<List<BotSignal>> = repository.getAssetStatuses()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val goldCooldown: StateFlow<GoldCooldownState> = repository.listenToGoldCooldown()
        .catch { emit(GoldCooldownState()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GoldCooldownState()
        )

    fun toggleAsset(symbol: String, currentState: Boolean) {
        viewModelScope.launch {
            repository.updateAssetToggle(symbol, !currentState)
        }
    }

    fun masterKillSwitch(enableAll: Boolean) {
        viewModelScope.launch {
            repository.setMasterKillSwitch(enableAll, assetStatuses.value)
        }
    }
}
