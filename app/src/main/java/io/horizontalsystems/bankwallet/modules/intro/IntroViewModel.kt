package io.horizontalsystems.bankwallet.modules.intro

import androidx.lifecycle.ViewModel
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.ILocalStorage

class IntroViewModel(
        private val localStorage: ILocalStorage
): ViewModel() {

    val slides = listOf(
        IntroModule.IntroSliderData(
            R.string.Intro_Wallet_Screen2Title,
            R.string.Intro_Wallet_Screen2Description,
            R.drawable.undraw_crypto_portfolio_2jy5,
            R.drawable.undraw_crypto_portfolio_2jy5
        ),
        IntroModule.IntroSliderData(
            R.string.Intro_Wallet_Screen3Title,
            R.string.Intro_Wallet_Screen3Description,
            R.drawable.undraw_stock_prices_re_js33,
            R.drawable.undraw_stock_prices_re_js33
        ),
        IntroModule.IntroSliderData(
            R.string.Intro_Wallet_Screen4Title,
            R.string.Intro_Wallet_Screen4Description,
            R.drawable.undraw_security_on_re_e491,
            R.drawable.undraw_security_on_re_e491
        ),
    )

    fun onStartClicked() {
        localStorage.mainShowedOnce = true
    }

}
