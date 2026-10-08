package nerd.forex.ninjaroboautotrader.data.model

data class GoldCooldownState(
    val remaining: Long = 0L,
    val total: Long = 120L,
    val formattedTime: String = "00:00",
    val progress: Float = 0f
)
