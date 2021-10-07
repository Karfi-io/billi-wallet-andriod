package io.horizontalsystems.bankwallet.modules.coin.overview

import io.horizontalsystems.bankwallet.core.Clearable
import io.horizontalsystems.bankwallet.core.IChartTypeStorage
import io.horizontalsystems.bankwallet.core.IRateManager
import io.horizontalsystems.bankwallet.core.subscribeIO
import io.horizontalsystems.bankwallet.modules.coin.LastPoint
import io.horizontalsystems.core.entities.Currency
import io.horizontalsystems.marketkit.MarketKit
import io.horizontalsystems.marketkit.models.CoinPrice
import io.horizontalsystems.marketkit.models.CoinType
import io.horizontalsystems.marketkit.models.FullCoin
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.net.URL

class CoinOverviewService(
    private val fullCoin: FullCoin,
    val currency: Currency,
    private val xRateManager: IRateManager,
    private val marketKit: MarketKit,
    private val chartTypeStorage: IChartTypeStorage,
    guidesBaseUrl: String,
) : Clearable {

    sealed class CoinDetailsState {
        object Loading : CoinDetailsState()
        object Loaded : CoinDetailsState()
        data class Error(val error: Throwable) : CoinDetailsState()
    }

    val coinUid get() = fullCoin.coin.uid
    var coinType: CoinType = fullCoin.platforms.first().coinType

    val coinPriceAsync = BehaviorSubject.create<CoinPrice>()
    val chartInfoUpdatedObservable: BehaviorSubject<Unit> = BehaviorSubject.create()
    val chartSpinnerObservable: BehaviorSubject<Unit> = BehaviorSubject.create()
    val chartInfoErrorObservable: BehaviorSubject<Throwable> = BehaviorSubject.create()
    val coinDetailsStateObservable: BehaviorSubject<CoinDetailsState> = BehaviorSubject.createDefault(
        CoinDetailsState.Loading)
    val topTokenHoldersStateObservable: BehaviorSubject<CoinDetailsState> = BehaviorSubject.createDefault(
        CoinDetailsState.Loading)

    var coinMarketDetails: CoinMarketDetails? = null
    var topTokenHolders: List<TokenHolder> = listOf()

    var lastPoint: LastPoint? = marketKit.coinPrice(fullCoin.coin.uid, currency.code)?.let { LastPoint(it.value, it.timestamp, it.diff) }
        set(value) {
            field = value
            triggerChartUpdateIfEnoughData()
        }

    var chartInfo: ChartInfo? = null
        set(value) {
            field = value
            triggerChartUpdateIfEnoughData()
        }

    private fun triggerChartUpdateIfEnoughData() {
        if (chartInfo != null && lastPoint != null) {
            chartInfoUpdatedObservable.onNext(Unit)
        }
    }

    private val disposables = CompositeDisposable()
    private var chartInfoDisposable: Disposable? = null

    init {
        marketKit.coinPrice(fullCoin.coin.uid, currency.code)?.let {
            coinPriceAsync.onNext(it)
        }
        marketKit.coinPriceObservable(fullCoin.coin.uid, currency.code)
                .subscribeIO {
                    coinPriceAsync.onNext(it)
                }
                .let {
                    disposables.add(it)
                }
        marketKit.coinPriceObservable(fullCoin.coin.uid, currency.code)
                .subscribeIO({ marketInfo ->
                    lastPoint = LastPoint(marketInfo.value, marketInfo.timestamp, marketInfo.diff)
                }, {
                    //ignore
                }).let {
                    disposables.add(it)
                }
    }

    var chartType: ChartType
        get() = chartTypeStorage.chartType ?: ChartType.TODAY
        set(value) {
            chartTypeStorage.chartType = value
        }

    private val guidesBaseUrl = URL(guidesBaseUrl)

    val guideUrl: String?
        get() {
            val guideRelativeUrl = when (val coinType = coinType) {
                CoinType.Bitcoin -> "guides/token_guides/en/bitcoin.md"
                CoinType.Ethereum -> "guides/token_guides/en/ethereum.md"
                CoinType.BitcoinCash -> "guides/token_guides/en/bitcoin-cash.md"
                CoinType.Zcash -> "guides/token_guides/en/zcash.md"
                is CoinType.Erc20 -> {
                    when (coinType.address) {
                        "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984" -> "guides/token_guides/en/uniswap.md"
                        "0xd533a949740bb3306d119cc777fa900ba034cd52" -> "guides/token_guides/en/curve-finance.md"
                        "0xba100000625a3754423978a60c9317c58a424e3d" -> "guides/token_guides/en/balancer-dex.md"
                        "0xc011a73ee8576fb46f5e1c5751ca3b9fe0af2a6f" -> "guides/token_guides/en/synthetix.md"
                        "0xdac17f958d2ee523a2206206994597c13d831ec7" -> "guides/token_guides/en/tether.md"
                        "0x9f8f72aa9304c8b593d555f12ef6589cc3a579a2" -> "guides/token_guides/en/makerdao.md"
                        "0x6b175474e89094c44da98b954eedeac495271d0f" -> "guides/token_guides/en/makerdao.md"
                        "0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9" -> "guides/token_guides/en/aave.md"
                        "0xc00e94cb662c3520282e6f5717214004a7f26888" -> "guides/token_guides/en/compound.md"
                        else -> null
                    }
                }
                else -> null
            }
            return guideRelativeUrl?.let {
                URL(guidesBaseUrl, it).toString()
            }
        }

    fun getCoinDetails(rateDiffCoinCodes: List<String>, rateDiffPeriods: List<TimePeriod>) {
        coinDetailsStateObservable.onNext(CoinDetailsState.Loading)
        xRateManager.coinMarketDetailsAsync(coinType, currency.code, rateDiffCoinCodes, rateDiffPeriods)
                .subscribeIO({ coinMarketDetails ->
                    this.coinMarketDetails = coinMarketDetails
                    coinDetailsStateObservable.onNext(CoinDetailsState.Loaded)
                }, {
                    coinDetailsStateObservable.onNext(CoinDetailsState.Error(it))
                }).let {
                    disposables.add(it)
                }
    }

    fun getTopTokenHolders(){
        xRateManager.getTopTokenHoldersAsync(coinType)
                .subscribeIO({ topTokenHolders ->
                    this.topTokenHolders = topTokenHolders
                    topTokenHoldersStateObservable.onNext(CoinDetailsState.Loaded)
                }, {
                    topTokenHoldersStateObservable.onNext(CoinDetailsState.Error(it))
                }).let {
                    disposables.add(it)
                }
    }

    fun updateChartInfo() {
        chartInfoDisposable?.dispose()

        chartInfo = xRateManager.chartInfo(coinType, currency.code, chartType)
        if (chartInfo == null){
                //show chart spinner only when chart data is not locally cached
                // and we need to wait for network response for data
            chartSpinnerObservable.onNext(Unit)
        }
        xRateManager.chartInfoObservable(coinType, currency.code, chartType)
                .subscribeIO({ chartInfo ->
                    this.chartInfo = chartInfo
                }, {
                    chartInfoErrorObservable.onNext(it)
                }).let {
                    chartInfoDisposable = it
                }
    }

    override fun clear() {
        chartInfoDisposable?.dispose()
        disposables.clear()
    }
}