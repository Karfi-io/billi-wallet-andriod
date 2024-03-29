package io.horizontalsystems.bankwallet.core.providers

import androidx.annotation.StringRes

object Translator {

    fun getString(@StringRes id: Int): String {
        return io.horizontalsystems.bankwallet.core.App.instance.localizedContext().getString(id)
    }

    fun getString(@StringRes id: Int, vararg params: Any): String {
        return io.horizontalsystems.bankwallet.core.App.instance.localizedContext().getString(id, *params)
    }
}
