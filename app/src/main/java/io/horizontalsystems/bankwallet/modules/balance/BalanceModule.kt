package io.horizontalsystems.bankwallet.modules.balance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.core.AdapterState
import io.horizontalsystems.bankwallet.core.BalanceData
import io.horizontalsystems.bankwallet.entities.Wallet
import io.horizontalsystems.bankwallet.modules.address.AddressHandlerFactory
import io.horizontalsystems.bankwallet.modules.balance.cex.BalanceCexRepositoryWrapper
import io.horizontalsystems.bankwallet.modules.balance.cex.BalanceCexSorter
import io.horizontalsystems.bankwallet.modules.balance.cex.BalanceCexViewModel
import io.horizontalsystems.marketkit.models.CoinPrice

object BalanceModule {
    class AccountsFactory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BalanceAccountsViewModel(io.horizontalsystems.bankwallet.core.App.accountManager) as T
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val totalService = TotalService(
                io.horizontalsystems.bankwallet.core.App.currencyManager,
                io.horizontalsystems.bankwallet.core.App.marketKit,
                io.horizontalsystems.bankwallet.core.App.baseTokenManager,
                io.horizontalsystems.bankwallet.core.App.balanceHiddenManager
            )
            return BalanceViewModel(
                BalanceService.getInstance("wallet"),
                BalanceViewItemFactory(),
                io.horizontalsystems.bankwallet.core.App.balanceViewTypeManager,
                TotalBalance(totalService, io.horizontalsystems.bankwallet.core.App.balanceHiddenManager),
                io.horizontalsystems.bankwallet.core.App.localStorage,
                io.horizontalsystems.bankwallet.core.App.wc2Service,
                io.horizontalsystems.bankwallet.core.App.wc2Manager,
                AddressHandlerFactory(io.horizontalsystems.bankwallet.core.App.appConfigProvider.udnApiKey),
            ) as T
        }
    }

    class FactoryCex : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val totalService = TotalService(
                io.horizontalsystems.bankwallet.core.App.currencyManager,
                io.horizontalsystems.bankwallet.core.App.marketKit,
                io.horizontalsystems.bankwallet.core.App.baseTokenManager,
                io.horizontalsystems.bankwallet.core.App.balanceHiddenManager
            )

            return BalanceCexViewModel(
                TotalBalance(totalService, io.horizontalsystems.bankwallet.core.App.balanceHiddenManager),
                io.horizontalsystems.bankwallet.core.App.localStorage,
                io.horizontalsystems.bankwallet.core.App.balanceViewTypeManager,
                BalanceViewItemFactory(),
                BalanceCexRepositoryWrapper(io.horizontalsystems.bankwallet.core.App.cexAssetManager, io.horizontalsystems.bankwallet.core.App.connectivityManager),
                BalanceXRateRepository("wallet", io.horizontalsystems.bankwallet.core.App.currencyManager, io.horizontalsystems.bankwallet.core.App.marketKit),
                BalanceCexSorter(),
                io.horizontalsystems.bankwallet.core.App.cexProviderManager,
            ) as T
        }
    }

    data class BalanceItem(
        val wallet: Wallet,
        val balanceData: BalanceData,
        val state: AdapterState,
        val sendAllowed: Boolean,
        val coinPrice: CoinPrice? = null
    ) {
        val fiatValue get() = coinPrice?.value?.let { balanceData.available.times(it) }
        val balanceFiatTotal get() = coinPrice?.value?.let { balanceData.total.times(it) }
    }
}