package io.horizontalsystems.bankwallet.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class CurrencyValue(val currency: Currency, val value: BigDecimal) : Parcelable {
    fun getFormattedFull(): String {
        return io.horizontalsystems.bankwallet.core.App.numberFormatter.formatFiatFull(value, currency.symbol)
    }

    fun getFormattedShort(): String {
        return io.horizontalsystems.bankwallet.core.App.numberFormatter.formatFiatShort(value, currency.symbol, 2)
    }
}
