package io.horizontalsystems.bankwallet.core

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import io.horizontalsystems.bankwallet.core.factories.AccountFactory
import io.horizontalsystems.bankwallet.core.factories.AdapterFactory
import io.horizontalsystems.bankwallet.core.factories.EvmAccountManagerFactory
import io.horizontalsystems.bankwallet.core.managers.AccountCleaner
import io.horizontalsystems.bankwallet.core.managers.AccountManager
import io.horizontalsystems.bankwallet.core.managers.AdapterManager
import io.horizontalsystems.bankwallet.core.managers.AppVersionManager
import io.horizontalsystems.bankwallet.core.managers.BackgroundStateChangeListener
import io.horizontalsystems.bankwallet.core.managers.BackupManager
import io.horizontalsystems.bankwallet.core.managers.BalanceHiddenManager
import io.horizontalsystems.bankwallet.core.managers.BaseTokenManager
import io.horizontalsystems.bankwallet.core.managers.BinanceKitManager
import io.horizontalsystems.bankwallet.core.managers.BtcBlockchainManager
import io.horizontalsystems.bankwallet.core.managers.CexAssetManager
import io.horizontalsystems.bankwallet.core.managers.CoinManager
import io.horizontalsystems.bankwallet.core.managers.ConnectivityManager
import io.horizontalsystems.bankwallet.core.managers.CurrencyManager
import io.horizontalsystems.bankwallet.core.managers.EvmBlockchainManager
import io.horizontalsystems.bankwallet.core.managers.EvmLabelManager
import io.horizontalsystems.bankwallet.core.managers.EvmSyncSourceManager
import io.horizontalsystems.bankwallet.core.managers.KeyStoreCleaner
import io.horizontalsystems.bankwallet.core.managers.LanguageManager
import io.horizontalsystems.bankwallet.core.managers.LocalStorageManager
import io.horizontalsystems.bankwallet.core.managers.MarketFavoritesManager
import io.horizontalsystems.bankwallet.core.managers.MarketKitWrapper
import io.horizontalsystems.bankwallet.core.managers.NetworkManager
import io.horizontalsystems.bankwallet.core.managers.NftAdapterManager
import io.horizontalsystems.bankwallet.core.managers.NftMetadataManager
import io.horizontalsystems.bankwallet.core.managers.NftMetadataSyncer
import io.horizontalsystems.bankwallet.core.managers.NumberFormatter
import io.horizontalsystems.bankwallet.core.managers.RateAppManager
import io.horizontalsystems.bankwallet.core.managers.ReleaseNotesManager
import io.horizontalsystems.bankwallet.core.managers.RestoreSettingsManager
import io.horizontalsystems.bankwallet.core.managers.SolanaKitManager
import io.horizontalsystems.bankwallet.core.managers.SolanaRpcSourceManager
import io.horizontalsystems.bankwallet.core.managers.SolanaWalletManager
import io.horizontalsystems.bankwallet.core.managers.SpamManager
import io.horizontalsystems.bankwallet.core.managers.SubscriptionManager
import io.horizontalsystems.bankwallet.core.managers.SystemInfoManager
import io.horizontalsystems.bankwallet.core.managers.TermsManager
import io.horizontalsystems.bankwallet.core.managers.TokenAutoEnableManager
import io.horizontalsystems.bankwallet.core.managers.TorManager
import io.horizontalsystems.bankwallet.core.managers.TransactionAdapterManager
import io.horizontalsystems.bankwallet.core.managers.TronAccountManager
import io.horizontalsystems.bankwallet.core.managers.TronKitManager
import io.horizontalsystems.bankwallet.core.managers.UserManager
import io.horizontalsystems.bankwallet.core.managers.WalletActivator
import io.horizontalsystems.bankwallet.core.managers.WalletManager
import io.horizontalsystems.bankwallet.core.managers.WalletStorage
import io.horizontalsystems.bankwallet.core.managers.WordsManager
import io.horizontalsystems.bankwallet.core.managers.ZcashBirthdayProvider
import io.horizontalsystems.bankwallet.core.providers.AppConfigProvider
import io.horizontalsystems.bankwallet.core.providers.CexProviderManager
import io.horizontalsystems.bankwallet.core.providers.EvmLabelProvider
import io.horizontalsystems.bankwallet.core.providers.FeeRateProvider
import io.horizontalsystems.bankwallet.core.providers.FeeTokenProvider
import io.horizontalsystems.bankwallet.core.storage.AccountsStorage
import io.horizontalsystems.bankwallet.core.storage.AppDatabase
import io.horizontalsystems.bankwallet.core.storage.BlockchainSettingsStorage
import io.horizontalsystems.bankwallet.core.storage.EnabledWalletsStorage
import io.horizontalsystems.bankwallet.core.storage.EvmSyncSourceStorage
import io.horizontalsystems.bankwallet.core.storage.NftStorage
import io.horizontalsystems.bankwallet.core.storage.RestoreSettingsStorage
import io.horizontalsystems.bankwallet.modules.backuplocal.fullbackup.BackupProvider
import io.horizontalsystems.bankwallet.modules.balance.BalanceViewTypeManager
import io.horizontalsystems.bankwallet.modules.chart.ChartIndicatorManager
import io.horizontalsystems.bankwallet.modules.contacts.ContactsRepository
import io.horizontalsystems.bankwallet.modules.keystore.KeyStoreActivity
import io.horizontalsystems.bankwallet.modules.launcher.LauncherActivity
import io.horizontalsystems.bankwallet.modules.lockscreen.LockScreenActivity
import io.horizontalsystems.bankwallet.modules.market.favorites.MarketFavoritesMenuService
import io.horizontalsystems.bankwallet.modules.market.topnftcollections.TopNftCollectionsRepository
import io.horizontalsystems.bankwallet.modules.market.topnftcollections.TopNftCollectionsViewItemFactory
import io.horizontalsystems.bankwallet.modules.market.topplatforms.TopPlatformsRepository
import io.horizontalsystems.bankwallet.modules.pin.PinComponent
import io.horizontalsystems.bankwallet.modules.pin.core.PinDbStorage
import io.horizontalsystems.bankwallet.modules.profeatures.ProFeaturesAuthorizationManager
import io.horizontalsystems.bankwallet.modules.profeatures.storage.ProFeaturesStorage
import io.horizontalsystems.bankwallet.modules.settings.appearance.AppIconService
import io.horizontalsystems.bankwallet.modules.settings.appearance.LaunchScreenService
import io.horizontalsystems.bankwallet.modules.theme.ThemeService
import io.horizontalsystems.bankwallet.modules.theme.ThemeType
import io.horizontalsystems.bankwallet.modules.walletconnect.storage.WC2SessionStorage
import io.horizontalsystems.bankwallet.modules.walletconnect.version2.WC2Manager
import io.horizontalsystems.bankwallet.modules.walletconnect.version2.WC2Service
import io.horizontalsystems.bankwallet.modules.walletconnect.version2.WC2SessionManager
import io.horizontalsystems.bankwallet.widgets.MarketWidgetManager
import io.horizontalsystems.bankwallet.widgets.MarketWidgetRepository
import io.horizontalsystems.bankwallet.widgets.MarketWidgetWorker
import io.horizontalsystems.core.BackgroundManager
import io.horizontalsystems.core.CoreApp
import io.horizontalsystems.core.ICoreApp
import io.horizontalsystems.core.security.EncryptionManager
import io.horizontalsystems.core.security.KeyStoreManager
import io.horizontalsystems.bankwallet.BuildConfig
import io.horizontalsystems.ethereumkit.core.EthereumKit
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.reactivex.plugins.RxJavaPlugins
import java.util.logging.Level
import java.util.logging.Logger
import androidx.work.Configuration as WorkConfiguration

class App : CoreApp(), WorkConfiguration.Provider, ImageLoaderFactory {

    companion object : ICoreApp by CoreApp {

        lateinit var preferences: SharedPreferences
        lateinit var feeRateProvider: FeeRateProvider
        lateinit var localStorage: io.horizontalsystems.bankwallet.core.ILocalStorage
        lateinit var marketStorage: io.horizontalsystems.bankwallet.core.IMarketStorage
        lateinit var torKitManager: io.horizontalsystems.bankwallet.core.ITorManager
        lateinit var restoreSettingsStorage: io.horizontalsystems.bankwallet.core.IRestoreSettingsStorage
        lateinit var currencyManager: CurrencyManager
        lateinit var languageManager: LanguageManager

        lateinit var blockchainSettingsStorage: BlockchainSettingsStorage
        lateinit var evmSyncSourceStorage: EvmSyncSourceStorage
        lateinit var btcBlockchainManager: BtcBlockchainManager
        lateinit var wordsManager: WordsManager
        lateinit var networkManager: io.horizontalsystems.bankwallet.core.INetworkManager
        lateinit var backgroundStateChangeListener: BackgroundStateChangeListener
        lateinit var appConfigProvider: AppConfigProvider
        lateinit var adapterManager: io.horizontalsystems.bankwallet.core.IAdapterManager
        lateinit var transactionAdapterManager: TransactionAdapterManager
        lateinit var walletManager: io.horizontalsystems.bankwallet.core.IWalletManager
        lateinit var walletActivator: WalletActivator
        lateinit var tokenAutoEnableManager: TokenAutoEnableManager
        lateinit var walletStorage: io.horizontalsystems.bankwallet.core.IWalletStorage
        lateinit var accountManager: io.horizontalsystems.bankwallet.core.IAccountManager
        lateinit var userManager: UserManager
        lateinit var accountFactory: io.horizontalsystems.bankwallet.core.IAccountFactory
        lateinit var backupManager: io.horizontalsystems.bankwallet.core.IBackupManager
        lateinit var proFeatureAuthorizationManager: ProFeaturesAuthorizationManager
        lateinit var zcashBirthdayProvider: ZcashBirthdayProvider

        lateinit var connectivityManager: ConnectivityManager
        lateinit var appDatabase: AppDatabase
        lateinit var accountsStorage: io.horizontalsystems.bankwallet.core.IAccountsStorage
        lateinit var enabledWalletsStorage: io.horizontalsystems.bankwallet.core.IEnabledWalletStorage
        lateinit var binanceKitManager: BinanceKitManager
        lateinit var solanaKitManager: SolanaKitManager
        lateinit var tronKitManager: TronKitManager
        lateinit var numberFormatter: io.horizontalsystems.bankwallet.core.IAppNumberFormatter
        lateinit var feeCoinProvider: FeeTokenProvider
        lateinit var accountCleaner: io.horizontalsystems.bankwallet.core.IAccountCleaner
        lateinit var rateAppManager: io.horizontalsystems.bankwallet.core.IRateAppManager
        lateinit var coinManager: io.horizontalsystems.bankwallet.core.ICoinManager
        lateinit var wc2Service: WC2Service
        lateinit var wc2SessionManager: WC2SessionManager
        lateinit var wc2Manager: WC2Manager
        lateinit var termsManager: io.horizontalsystems.bankwallet.core.ITermsManager
        lateinit var marketFavoritesManager: MarketFavoritesManager
        lateinit var marketKit: MarketKitWrapper
        lateinit var releaseNotesManager: ReleaseNotesManager
        lateinit var restoreSettingsManager: RestoreSettingsManager
        lateinit var evmSyncSourceManager: EvmSyncSourceManager
        lateinit var evmBlockchainManager: EvmBlockchainManager
        lateinit var solanaRpcSourceManager: SolanaRpcSourceManager
        lateinit var nftMetadataManager: NftMetadataManager
        lateinit var nftAdapterManager: NftAdapterManager
        lateinit var nftMetadataSyncer: NftMetadataSyncer
        lateinit var evmLabelManager: EvmLabelManager
        lateinit var baseTokenManager: BaseTokenManager
        lateinit var balanceViewTypeManager: BalanceViewTypeManager
        lateinit var balanceHiddenManager: BalanceHiddenManager
        lateinit var marketWidgetManager: MarketWidgetManager
        lateinit var marketWidgetRepository: MarketWidgetRepository
        lateinit var contactsRepository: ContactsRepository
        lateinit var subscriptionManager: SubscriptionManager
        lateinit var cexProviderManager: CexProviderManager
        lateinit var cexAssetManager: CexAssetManager
        lateinit var chartIndicatorManager: ChartIndicatorManager
        lateinit var backupProvider: BackupProvider
        lateinit var spamManager: SpamManager
    }

    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG) {
            //Disable logging for lower levels in Release build
            Logger.getLogger("").level = Level.SEVERE
        }

        RxJavaPlugins.setErrorHandler { e: Throwable? ->
            Log.w("RxJava ErrorHandler", e)
        }

        EthereumKit.init()

        instance = this
        io.horizontalsystems.bankwallet.core.App.Companion.preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        LocalStorageManager(io.horizontalsystems.bankwallet.core.App.Companion.preferences).apply {
            io.horizontalsystems.bankwallet.core.App.Companion.localStorage = this
            pinSettingsStorage = this
            lockoutStorage = this
            thirdKeyboardStorage = this
            io.horizontalsystems.bankwallet.core.App.Companion.marketStorage = this
        }

        val appConfig = AppConfigProvider(io.horizontalsystems.bankwallet.core.App.Companion.localStorage)
        io.horizontalsystems.bankwallet.core.App.Companion.appConfigProvider = appConfig

        io.horizontalsystems.bankwallet.core.App.Companion.torKitManager = TorManager(instance,
            io.horizontalsystems.bankwallet.core.App.Companion.localStorage
        )
       io.horizontalsystems.bankwallet.core.App.Companion.subscriptionManager = SubscriptionManager()

        io.horizontalsystems.bankwallet.core.App.Companion.marketKit = MarketKitWrapper(
            context = this,
            hsApiBaseUrl = appConfig.marketApiBaseUrl,
            hsApiKey = appConfig.marketApiKey,
            cryptoCompareApiKey = appConfig.cryptoCompareApiKey,
            defiYieldApiKey = appConfig.defiyieldProviderApiKey,
            appConfigProvider = io.horizontalsystems.bankwallet.core.App.Companion.appConfigProvider,
            subscriptionManager = io.horizontalsystems.bankwallet.core.App.Companion.subscriptionManager
        )
        io.horizontalsystems.bankwallet.core.App.Companion.marketKit.sync()

        io.horizontalsystems.bankwallet.core.App.Companion.feeRateProvider = FeeRateProvider(io.horizontalsystems.bankwallet.core.App.Companion.appConfigProvider)
        backgroundManager = BackgroundManager(this)

        io.horizontalsystems.bankwallet.core.App.Companion.appDatabase = AppDatabase.getInstance(this)

        io.horizontalsystems.bankwallet.core.App.Companion.blockchainSettingsStorage = BlockchainSettingsStorage(
            io.horizontalsystems.bankwallet.core.App.Companion.appDatabase
        )
        io.horizontalsystems.bankwallet.core.App.Companion.evmSyncSourceStorage = EvmSyncSourceStorage(
            io.horizontalsystems.bankwallet.core.App.Companion.appDatabase
        )
        io.horizontalsystems.bankwallet.core.App.Companion.evmSyncSourceManager = EvmSyncSourceManager(
            io.horizontalsystems.bankwallet.core.App.Companion.appConfigProvider,
            io.horizontalsystems.bankwallet.core.App.Companion.blockchainSettingsStorage,
            io.horizontalsystems.bankwallet.core.App.Companion.evmSyncSourceStorage
        )

       io.horizontalsystems.bankwallet.core.App.Companion.btcBlockchainManager = BtcBlockchainManager(
           io.horizontalsystems.bankwallet.core.App.Companion.blockchainSettingsStorage,
           io.horizontalsystems.bankwallet.core.App.Companion.appConfigProvider,
           io.horizontalsystems.bankwallet.core.App.Companion.marketKit
        )

        io.horizontalsystems.bankwallet.core.App.Companion.binanceKitManager = BinanceKitManager()

        io.horizontalsystems.bankwallet.core.App.Companion.accountsStorage = AccountsStorage(io.horizontalsystems.bankwallet.core.App.Companion.appDatabase)
        io.horizontalsystems.bankwallet.core.App.Companion.restoreSettingsStorage = RestoreSettingsStorage(
            io.horizontalsystems.bankwallet.core.App.Companion.appDatabase
        )

        io.horizontalsystems.bankwallet.core.AppLog.logsDao = io.horizontalsystems.bankwallet.core.App.Companion.appDatabase.logsDao()

        io.horizontalsystems.bankwallet.core.App.Companion.accountCleaner = AccountCleaner()
        io.horizontalsystems.bankwallet.core.App.Companion.accountManager = AccountManager(
            io.horizontalsystems.bankwallet.core.App.Companion.accountsStorage,
            io.horizontalsystems.bankwallet.core.App.Companion.accountCleaner
        )
      io.horizontalsystems.bankwallet.core.App.Companion.userManager = UserManager(io.horizontalsystems.bankwallet.core.App.Companion.accountManager)

        val proFeaturesStorage = ProFeaturesStorage(io.horizontalsystems.bankwallet.core.App.Companion.appDatabase)
        io.horizontalsystems.bankwallet.core.App.Companion.proFeatureAuthorizationManager = ProFeaturesAuthorizationManager(proFeaturesStorage,
            io.horizontalsystems.bankwallet.core.App.Companion.accountManager,
            io.horizontalsystems.bankwallet.core.App.Companion.appConfigProvider
        )

        io.horizontalsystems.bankwallet.core.App.Companion.enabledWalletsStorage = EnabledWalletsStorage(
            io.horizontalsystems.bankwallet.core.App.Companion.appDatabase
        )
        io.horizontalsystems.bankwallet.core.App.Companion.walletStorage = WalletStorage(
            io.horizontalsystems.bankwallet.core.App.Companion.marketKit,
            io.horizontalsystems.bankwallet.core.App.Companion.enabledWalletsStorage
        )

        io.horizontalsystems.bankwallet.core.App.Companion.walletManager = WalletManager(
            io.horizontalsystems.bankwallet.core.App.Companion.accountManager,
            io.horizontalsystems.bankwallet.core.App.Companion.walletStorage
        )
        io.horizontalsystems.bankwallet.core.App.Companion.coinManager = CoinManager(
            io.horizontalsystems.bankwallet.core.App.Companion.marketKit,
            io.horizontalsystems.bankwallet.core.App.Companion.walletManager
        )

        io.horizontalsystems.bankwallet.core.App.Companion.solanaRpcSourceManager = SolanaRpcSourceManager(
            io.horizontalsystems.bankwallet.core.App.Companion.blockchainSettingsStorage,
            io.horizontalsystems.bankwallet.core.App.Companion.marketKit
        )
        val solanaWalletManager = SolanaWalletManager(
            io.horizontalsystems.bankwallet.core.App.Companion.walletManager,
            io.horizontalsystems.bankwallet.core.App.Companion.accountManager,
            io.horizontalsystems.bankwallet.core.App.Companion.marketKit
        )
        io.horizontalsystems.bankwallet.core.App.Companion.solanaKitManager = SolanaKitManager(
            io.horizontalsystems.bankwallet.core.App.Companion.appConfigProvider,
            io.horizontalsystems.bankwallet.core.App.Companion.solanaRpcSourceManager, solanaWalletManager, backgroundManager)

        io.horizontalsystems.bankwallet.core.App.Companion.tronKitManager = TronKitManager(io.horizontalsystems.bankwallet.core.App.Companion.appConfigProvider, backgroundManager)

        io.horizontalsystems.bankwallet.core.App.Companion.wordsManager = WordsManager(Mnemonic())
        io.horizontalsystems.bankwallet.core.App.Companion.networkManager = NetworkManager()
        io.horizontalsystems.bankwallet.core.App.Companion.accountFactory = AccountFactory(
            io.horizontalsystems.bankwallet.core.App.Companion.accountManager,
            io.horizontalsystems.bankwallet.core.App.Companion.userManager
        )
        io.horizontalsystems.bankwallet.core.App.Companion.backupManager = BackupManager(io.horizontalsystems.bankwallet.core.App.Companion.accountManager)


        KeyStoreManager(
            keyAlias = "MASTER_KEY",
            keyStoreCleaner = KeyStoreCleaner(
                io.horizontalsystems.bankwallet.core.App.Companion.localStorage,
                io.horizontalsystems.bankwallet.core.App.Companion.accountManager,
                io.horizontalsystems.bankwallet.core.App.Companion.walletManager
            ),
            logger = io.horizontalsystems.bankwallet.core.AppLogger("key-store")
        ).apply {
            keyStoreManager = this
            keyProvider = this
        }

        encryptionManager = EncryptionManager(keyProvider)

        io.horizontalsystems.bankwallet.core.App.Companion.walletActivator = WalletActivator(
            io.horizontalsystems.bankwallet.core.App.Companion.walletManager,
            io.horizontalsystems.bankwallet.core.App.Companion.marketKit
        )
        io.horizontalsystems.bankwallet.core.App.Companion.tokenAutoEnableManager = TokenAutoEnableManager(
            io.horizontalsystems.bankwallet.core.App.Companion.appDatabase.tokenAutoEnabledBlockchainDao())

        val evmAccountManagerFactory = EvmAccountManagerFactory(

            io.horizontalsystems.bankwallet.core.App.Companion.accountManager,
            io.horizontalsystems.bankwallet.core.App.Companion.walletManager,
            io.horizontalsystems.bankwallet.core.App.Companion.marketKit,
            io.horizontalsystems.bankwallet.core.App.Companion.tokenAutoEnableManager
        )
       io.horizontalsystems.bankwallet.core.App.Companion.evmBlockchainManager = EvmBlockchainManager(
            backgroundManager,
           io.horizontalsystems.bankwallet.core.App.Companion.evmSyncSourceManager,
           io.horizontalsystems.bankwallet.core.App.Companion.marketKit,
            evmAccountManagerFactory,
        )

        val tronAccountManager = TronAccountManager(
            io.horizontalsystems.bankwallet.core.App.Companion.accountManager,
            io.horizontalsystems.bankwallet.core.App.Companion.walletManager,
            io.horizontalsystems.bankwallet.core.App.Companion.marketKit,
            io.horizontalsystems.bankwallet.core.App.Companion.tronKitManager,
            io.horizontalsystems.bankwallet.core.App.Companion.tokenAutoEnableManager
        )
        tronAccountManager.start()

        systemInfoManager = SystemInfoManager(io.horizontalsystems.bankwallet.core.App.Companion.appConfigProvider)

        io.horizontalsystems.bankwallet.core.App.Companion.languageManager = LanguageManager()
        io.horizontalsystems.bankwallet.core.App.Companion.currencyManager = CurrencyManager(
            io.horizontalsystems.bankwallet.core.App.Companion.localStorage,
            io.horizontalsystems.bankwallet.core.App.Companion.appConfigProvider
        )
        io.horizontalsystems.bankwallet.core.App.Companion.numberFormatter = NumberFormatter(io.horizontalsystems.bankwallet.core.App.Companion.languageManager)

        io.horizontalsystems.bankwallet.core.App.Companion.connectivityManager = ConnectivityManager(backgroundManager)

       io.horizontalsystems.bankwallet.core.App.Companion.zcashBirthdayProvider = ZcashBirthdayProvider(this)
        io.horizontalsystems.bankwallet.core.App.Companion.restoreSettingsManager = RestoreSettingsManager(
            io.horizontalsystems.bankwallet.core.App.Companion.restoreSettingsStorage,
            io.horizontalsystems.bankwallet.core.App.Companion.zcashBirthdayProvider
        )

        io.horizontalsystems.bankwallet.core.App.Companion.evmLabelManager = EvmLabelManager(
            EvmLabelProvider(),
            io.horizontalsystems.bankwallet.core.App.Companion.appDatabase.evmAddressLabelDao(),
            io.horizontalsystems.bankwallet.core.App.Companion.appDatabase.evmMethodLabelDao(),
            io.horizontalsystems.bankwallet.core.App.Companion.appDatabase.syncerStateDao()
        )

        val adapterFactory = AdapterFactory(
            context = instance,
            btcBlockchainManager = io.horizontalsystems.bankwallet.core.App.Companion.btcBlockchainManager,
            evmBlockchainManager = io.horizontalsystems.bankwallet.core.App.Companion.evmBlockchainManager,
            evmSyncSourceManager = io.horizontalsystems.bankwallet.core.App.Companion.evmSyncSourceManager,
            binanceKitManager = io.horizontalsystems.bankwallet.core.App.Companion.binanceKitManager,
            solanaKitManager = io.horizontalsystems.bankwallet.core.App.Companion.solanaKitManager,
            tronKitManager = io.horizontalsystems.bankwallet.core.App.Companion.tronKitManager,
            backgroundManager = backgroundManager,
            restoreSettingsManager = io.horizontalsystems.bankwallet.core.App.Companion.restoreSettingsManager,
            coinManager = io.horizontalsystems.bankwallet.core.App.Companion.coinManager,
            evmLabelManager = io.horizontalsystems.bankwallet.core.App.Companion.evmLabelManager,
            localStorage = io.horizontalsystems.bankwallet.core.App.Companion.localStorage
        )
        io.horizontalsystems.bankwallet.core.App.Companion.adapterManager = AdapterManager(
            io.horizontalsystems.bankwallet.core.App.Companion.walletManager, adapterFactory,
            io.horizontalsystems.bankwallet.core.App.Companion.btcBlockchainManager,
            io.horizontalsystems.bankwallet.core.App.Companion.evmBlockchainManager,
            io.horizontalsystems.bankwallet.core.App.Companion.binanceKitManager,
            io.horizontalsystems.bankwallet.core.App.Companion.solanaKitManager,
            io.horizontalsystems.bankwallet.core.App.Companion.tronKitManager
        )
        io.horizontalsystems.bankwallet.core.App.Companion.transactionAdapterManager = TransactionAdapterManager(
            io.horizontalsystems.bankwallet.core.App.Companion.adapterManager, adapterFactory)

        io.horizontalsystems.bankwallet.core.App.Companion.feeCoinProvider = FeeTokenProvider(io.horizontalsystems.bankwallet.core.App.Companion.marketKit)

        pinComponent = PinComponent(
            pinSettingsStorage = pinSettingsStorage,
            excludedActivityNames = listOf(
                KeyStoreActivity::class.java.name,
                LockScreenActivity::class.java.name,
                LauncherActivity::class.java.name,
            ),
            userManager = io.horizontalsystems.bankwallet.core.App.Companion.userManager,
            pinDbStorage = PinDbStorage(io.horizontalsystems.bankwallet.core.App.Companion.appDatabase.pinDao())
        )

        io.horizontalsystems.bankwallet.core.App.Companion.backgroundStateChangeListener = BackgroundStateChangeListener(systemInfoManager, keyStoreManager, pinComponent).apply {
            backgroundManager.registerListener(this)
        }

        io.horizontalsystems.bankwallet.core.App.Companion.rateAppManager = RateAppManager(
            io.horizontalsystems.bankwallet.core.App.Companion.walletManager,
            io.horizontalsystems.bankwallet.core.App.Companion.adapterManager,
            io.horizontalsystems.bankwallet.core.App.Companion.localStorage
        )

        io.horizontalsystems.bankwallet.core.App.Companion.wc2Manager = WC2Manager(
            io.horizontalsystems.bankwallet.core.App.Companion.accountManager,
            io.horizontalsystems.bankwallet.core.App.Companion.evmBlockchainManager
        )

        io.horizontalsystems.bankwallet.core.App.Companion.termsManager = TermsManager(io.horizontalsystems.bankwallet.core.App.Companion.localStorage)

        io.horizontalsystems.bankwallet.core.App.Companion.marketWidgetManager = MarketWidgetManager()
        io.horizontalsystems.bankwallet.core.App.Companion.marketFavoritesManager = MarketFavoritesManager(
            io.horizontalsystems.bankwallet.core.App.Companion.appDatabase,
            io.horizontalsystems.bankwallet.core.App.Companion.marketWidgetManager
        )

        io.horizontalsystems.bankwallet.core.App.Companion.marketWidgetRepository = MarketWidgetRepository(
            io.horizontalsystems.bankwallet.core.App.Companion.marketKit,
            io.horizontalsystems.bankwallet.core.App.Companion.marketFavoritesManager,
            MarketFavoritesMenuService(
                io.horizontalsystems.bankwallet.core.App.Companion.localStorage,
                io.horizontalsystems.bankwallet.core.App.Companion.marketWidgetManager
            ),
            TopNftCollectionsRepository(io.horizontalsystems.bankwallet.core.App.Companion.marketKit),
            TopNftCollectionsViewItemFactory(io.horizontalsystems.bankwallet.core.App.Companion.numberFormatter),
            TopPlatformsRepository(
                io.horizontalsystems.bankwallet.core.App.Companion.marketKit,
                io.horizontalsystems.bankwallet.core.App.Companion.currencyManager, "widget"),
            io.horizontalsystems.bankwallet.core.App.Companion.currencyManager
        )

        io.horizontalsystems.bankwallet.core.App.Companion.releaseNotesManager = ReleaseNotesManager(systemInfoManager,
            io.horizontalsystems.bankwallet.core.App.Companion.localStorage,
            io.horizontalsystems.bankwallet.core.App.Companion.appConfigProvider
        )

        setAppTheme()

        val nftStorage = NftStorage(
            io.horizontalsystems.bankwallet.core.App.Companion.appDatabase.nftDao(),
            io.horizontalsystems.bankwallet.core.App.Companion.marketKit
        )
        io.horizontalsystems.bankwallet.core.App.Companion.nftMetadataManager = NftMetadataManager(
            io.horizontalsystems.bankwallet.core.App.Companion.marketKit,
            io.horizontalsystems.bankwallet.core.App.Companion.appConfigProvider, nftStorage)
       io.horizontalsystems.bankwallet.core.App.Companion.nftAdapterManager = NftAdapterManager(
           io.horizontalsystems.bankwallet.core.App.Companion.walletManager,
           io.horizontalsystems.bankwallet.core.App.Companion.evmBlockchainManager
        )
       io.horizontalsystems.bankwallet.core.App.Companion.nftMetadataSyncer = NftMetadataSyncer(
           io.horizontalsystems.bankwallet.core.App.Companion.nftAdapterManager,
           io.horizontalsystems.bankwallet.core.App.Companion.nftMetadataManager, nftStorage)

        initializeWalletConnectV2(appConfig)

        io.horizontalsystems.bankwallet.core.App.Companion.wc2Service = WC2Service()
        io.horizontalsystems.bankwallet.core.App.Companion.wc2SessionManager = WC2SessionManager(
            io.horizontalsystems.bankwallet.core.App.Companion.accountManager, WC2SessionStorage(
                io.horizontalsystems.bankwallet.core.App.Companion.appDatabase
            ),
            io.horizontalsystems.bankwallet.core.App.Companion.wc2Service,
            io.horizontalsystems.bankwallet.core.App.Companion.wc2Manager
        )

        io.horizontalsystems.bankwallet.core.App.Companion.baseTokenManager = BaseTokenManager(
            io.horizontalsystems.bankwallet.core.App.Companion.coinManager,
            io.horizontalsystems.bankwallet.core.App.Companion.localStorage
        )
        io.horizontalsystems.bankwallet.core.App.Companion.balanceViewTypeManager = BalanceViewTypeManager(
            io.horizontalsystems.bankwallet.core.App.Companion.localStorage
        )
        io.horizontalsystems.bankwallet.core.App.Companion.balanceHiddenManager = BalanceHiddenManager(
            io.horizontalsystems.bankwallet.core.App.Companion.localStorage, backgroundManager)

        io.horizontalsystems.bankwallet.core.App.Companion.contactsRepository = ContactsRepository(
            io.horizontalsystems.bankwallet.core.App.Companion.marketKit
        )
        io.horizontalsystems.bankwallet.core.App.Companion.cexProviderManager = CexProviderManager(
            io.horizontalsystems.bankwallet.core.App.Companion.accountManager
        )
        io.horizontalsystems.bankwallet.core.App.Companion.cexAssetManager = CexAssetManager(io.horizontalsystems.bankwallet.core.App.Companion.marketKit, io.horizontalsystems.bankwallet.core.App.Companion.appDatabase.cexAssetsDao())
        io.horizontalsystems.bankwallet.core.App.Companion.chartIndicatorManager = ChartIndicatorManager(
            io.horizontalsystems.bankwallet.core.App.Companion.appDatabase.chartIndicatorSettingsDao(),
            io.horizontalsystems.bankwallet.core.App.Companion.localStorage
        )

        io.horizontalsystems.bankwallet.core.App.Companion.backupProvider = BackupProvider(
            localStorage = io.horizontalsystems.bankwallet.core.App.Companion.localStorage,
            languageManager = io.horizontalsystems.bankwallet.core.App.Companion.languageManager,
            walletStorage = io.horizontalsystems.bankwallet.core.App.Companion.enabledWalletsStorage,
            settingsManager = io.horizontalsystems.bankwallet.core.App.Companion.restoreSettingsManager,
            accountManager = io.horizontalsystems.bankwallet.core.App.Companion.accountManager,
            accountFactory = io.horizontalsystems.bankwallet.core.App.Companion.accountFactory,
            walletManager = io.horizontalsystems.bankwallet.core.App.Companion.walletManager,
            restoreSettingsManager = io.horizontalsystems.bankwallet.core.App.Companion.restoreSettingsManager,
            blockchainSettingsStorage = io.horizontalsystems.bankwallet.core.App.Companion.blockchainSettingsStorage,
            evmBlockchainManager = io.horizontalsystems.bankwallet.core.App.Companion.evmBlockchainManager,
            marketFavoritesManager = io.horizontalsystems.bankwallet.core.App.Companion.marketFavoritesManager,
            balanceViewTypeManager = io.horizontalsystems.bankwallet.core.App.Companion.balanceViewTypeManager,
            appIconService = AppIconService(io.horizontalsystems.bankwallet.core.App.Companion.localStorage),
            themeService = ThemeService(io.horizontalsystems.bankwallet.core.App.Companion.localStorage),
            chartIndicatorManager = io.horizontalsystems.bankwallet.core.App.Companion.chartIndicatorManager,
            chartIndicatorSettingsDao = io.horizontalsystems.bankwallet.core.App.Companion.appDatabase.chartIndicatorSettingsDao(),
            balanceHiddenManager = io.horizontalsystems.bankwallet.core.App.Companion.balanceHiddenManager,
            baseTokenManager = io.horizontalsystems.bankwallet.core.App.Companion.baseTokenManager,
            launchScreenService = LaunchScreenService(io.horizontalsystems.bankwallet.core.App.Companion.localStorage),
            currencyManager = io.horizontalsystems.bankwallet.core.App.Companion.currencyManager,
            btcBlockchainManager = io.horizontalsystems.bankwallet.core.App.Companion.btcBlockchainManager,
            evmSyncSourceManager = io.horizontalsystems.bankwallet.core.App.Companion.evmSyncSourceManager,
            evmSyncSourceStorage = io.horizontalsystems.bankwallet.core.App.Companion.evmSyncSourceStorage,
            solanaRpcSourceManager = io.horizontalsystems.bankwallet.core.App.Companion.solanaRpcSourceManager,
            contactsRepository = io.horizontalsystems.bankwallet.core.App.Companion.contactsRepository
        )

        io.horizontalsystems.bankwallet.core.App.Companion.spamManager = SpamManager(io.horizontalsystems.bankwallet.core.App.Companion.localStorage)

        startTasks()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .components {
                add(SvgDecoder.Factory())
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    private fun initializeWalletConnectV2(appConfig: AppConfigProvider) {
        val projectId = appConfig.walletConnectProjectId
        val serverUrl = "wss://${appConfig.walletConnectUrl}?projectId=$projectId"
        val connectionType = ConnectionType.AUTOMATIC
        val appMetaData = Core.Model.AppMetaData(
            name = appConfig.walletConnectAppMetaDataName,
            description = "",
            url = appConfig.walletConnectAppMetaDataUrl,
            icons = listOf(appConfig.walletConnectAppMetaDataIcon),
            redirect = null,
        )

        CoreClient.initialize(
            metaData = appMetaData,
            relayServerUrl = serverUrl,
            connectionType = connectionType,
            application = this,
            onError = { error ->
                Log.w("AAA", "error", error.throwable)
            },
        )

        val init = Sign.Params.Init(core = CoreClient)
        SignClient.initialize(init) { error ->
            Log.w("AAA", "error", error.throwable)
        }
    }

    private fun setAppTheme() {
        val nightMode = when (io.horizontalsystems.bankwallet.core.App.Companion.localStorage.currentTheme) {
            ThemeType.Light -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeType.Dark -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeType.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }

    override fun getWorkManagerConfiguration(): WorkConfiguration {
        return if (BuildConfig.DEBUG) {
            WorkConfiguration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .build()
        } else {
            WorkConfiguration.Builder()
                .setMinimumLoggingLevel(Log.ERROR)
                .build()
        }
    }

    override fun localizedContext(): Context {
        return localeAwareContext(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(localeAwareContext(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localeAwareContext(this)
    }

    private fun startTasks() {
        Thread {
            io.horizontalsystems.bankwallet.core.App.Companion.rateAppManager.onAppLaunch()
            io.horizontalsystems.bankwallet.core.App.Companion.nftMetadataSyncer.start()
            pinComponent.initDefaultPinLevel()
            io.horizontalsystems.bankwallet.core.App.Companion.accountManager.clearAccounts()

            AppVersionManager(systemInfoManager,
                io.horizontalsystems.bankwallet.core.App.Companion.localStorage
            ).apply { storeAppVersion() }

            if (MarketWidgetWorker.hasEnabledWidgets(instance)) {
                MarketWidgetWorker.enqueueWork(instance)
            } else {
                MarketWidgetWorker.cancel(instance)
            }

            io.horizontalsystems.bankwallet.core.App.Companion.evmLabelManager.sync()
            io.horizontalsystems.bankwallet.core.App.Companion.contactsRepository.initialize()

        }.start()
    }
}
