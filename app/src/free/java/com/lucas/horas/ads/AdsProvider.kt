package com.lucas.horas.ads

import android.app.Activity
import android.view.Gravity
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Variante "free" — mostra uma faixa de anúncio (AdMob) em baixo do ecrã principal.
 * IDs de TESTE da Google abaixo — trocar pelos reais da tua conta AdMob antes de publicar/vender.
 */
object AdsProvider {

    private const val TEST_BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    private var sdkInitialized = false

    fun prepareAndShowBanner(activity: Activity, container: FrameLayout) {
        val params = ConsentRequestParameters.Builder().build()
        val consentInfo = UserMessagingPlatform.getConsentInformation(activity)

        consentInfo.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    loadBanner(activity, container)
                }
            },
            {
                // Sem internet ou falha a obter o consentimento — mostra à mesma;
                // o AdMob usa anúncios não personalizados nesse caso.
                loadBanner(activity, container)
            }
        )
    }

    private fun loadBanner(activity: Activity, container: FrameLayout) {
        if (!sdkInitialized) {
            MobileAds.initialize(activity)
            sdkInitialized = true
        }

        val adView = AdView(activity)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = TEST_BANNER_UNIT_ID

        container.removeAllViews()
        container.addView(
            adView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
        adView.loadAd(AdRequest.Builder().build())
    }
}
