package nerd.forex.ninjaroboautotrader.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class AccountState(
    @get:PropertyName("equity")
    @set:PropertyName("equity")
    var equity: Double = 9575.70,

    @get:PropertyName("balance")
    @set:PropertyName("balance")
    var balance: Double = 9572.37,

    @get:PropertyName("margin")
    @set:PropertyName("margin")
    var margin: Double = 61.81,

    @get:PropertyName("leverage")
    @set:PropertyName("leverage")
    var leverage: Int = 500
)
