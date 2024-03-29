package io.horizontalsystems.bankwallet.modules.settings.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object MainSettingsModule {

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val service = MainSettingsService(
                io.horizontalsystems.bankwallet.core.App.backupManager,
                io.horizontalsystems.bankwallet.core.App.languageManager,
                io.horizontalsystems.bankwallet.core.App.systemInfoManager,
                io.horizontalsystems.bankwallet.core.App.currencyManager,
                io.horizontalsystems.bankwallet.core.App.termsManager,
                io.horizontalsystems.bankwallet.core.App.pinComponent,
                io.horizontalsystems.bankwallet.core.App.wc2SessionManager,
                io.horizontalsystems.bankwallet.core.App.wc2Manager,
                io.horizontalsystems.bankwallet.core.App.accountManager,
                io.horizontalsystems.bankwallet.core.App.appConfigProvider,
            )
            val viewModel = MainSettingsViewModel(
                service,
                io.horizontalsystems.bankwallet.core.App.appConfigProvider.companyWebPageLink,
            )

            return viewModel as T
        }
    }

    sealed class CounterType {
        class SessionCounter(val number: Int) : CounterType()
        class PendingRequestCounter(val number: Int) : CounterType()
    }

}
