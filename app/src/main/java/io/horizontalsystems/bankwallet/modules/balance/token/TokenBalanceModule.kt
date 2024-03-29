package io.horizontalsystems.bankwallet.modules.balance.token

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.entities.Wallet
import io.horizontalsystems.bankwallet.modules.balance.BalanceAdapterRepository
import io.horizontalsystems.bankwallet.modules.balance.BalanceCache
import io.horizontalsystems.bankwallet.modules.balance.BalanceViewItem
import io.horizontalsystems.bankwallet.modules.balance.BalanceViewItemFactory
import io.horizontalsystems.bankwallet.modules.balance.BalanceXRateRepository
import io.horizontalsystems.bankwallet.modules.transactions.NftMetadataService
import io.horizontalsystems.bankwallet.modules.transactions.TransactionRecordRepository
import io.horizontalsystems.bankwallet.modules.transactions.TransactionSyncStateRepository
import io.horizontalsystems.bankwallet.modules.transactions.TransactionViewItem
import io.horizontalsystems.bankwallet.modules.transactions.TransactionViewItemFactory
import io.horizontalsystems.bankwallet.modules.transactions.TransactionsRateRepository

class TokenBalanceModule {

    class Factory(private val wallet: Wallet) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val balanceService = TokenBalanceService(
                wallet,
                BalanceXRateRepository("wallet", io.horizontalsystems.bankwallet.core.App.currencyManager, io.horizontalsystems.bankwallet.core.App.marketKit),
                BalanceAdapterRepository(
                    io.horizontalsystems.bankwallet.core.App.adapterManager, BalanceCache(
                        io.horizontalsystems.bankwallet.core.App.appDatabase.enabledWalletsCacheDao())),
            )

            val tokenTransactionsService = TokenTransactionsService(
                wallet,
                TransactionRecordRepository(io.horizontalsystems.bankwallet.core.App.transactionAdapterManager),
                TransactionsRateRepository(io.horizontalsystems.bankwallet.core.App.currencyManager, io.horizontalsystems.bankwallet.core.App.marketKit),
                TransactionSyncStateRepository(io.horizontalsystems.bankwallet.core.App.transactionAdapterManager),
                io.horizontalsystems.bankwallet.core.App.contactsRepository,
                NftMetadataService(io.horizontalsystems.bankwallet.core.App.nftMetadataManager),
                io.horizontalsystems.bankwallet.core.App.spamManager
            )

            return TokenBalanceViewModel(
                wallet,
                balanceService,
                BalanceViewItemFactory(),
                tokenTransactionsService,
                TransactionViewItemFactory(io.horizontalsystems.bankwallet.core.App.evmLabelManager, io.horizontalsystems.bankwallet.core.App.contactsRepository, io.horizontalsystems.bankwallet.core.App.balanceHiddenManager),
                io.horizontalsystems.bankwallet.core.App.balanceHiddenManager,
                io.horizontalsystems.bankwallet.core.App.connectivityManager
            ) as T
        }
    }

    data class TokenBalanceUiState(
        val title: String,
        val balanceViewItem: BalanceViewItem?,
        val transactions: Map<String, List<TransactionViewItem>>?,
    )
}
