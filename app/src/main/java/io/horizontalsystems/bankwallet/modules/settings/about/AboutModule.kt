package io.horizontalsystems.bankwallet.modules.settings.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object AboutModule {
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AboutViewModel(io.horizontalsystems.bankwallet.core.App.appConfigProvider, io.horizontalsystems.bankwallet.core.App.termsManager, io.horizontalsystems.bankwallet.core.App.systemInfoManager) as T
        }
    }
}
