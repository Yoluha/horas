package com.lucas.horas.ads

import android.app.Activity
import android.view.View
import android.widget.FrameLayout

/** Variante "pro" — sem anúncios, sem SDK de anúncios incluído no APK. */
object AdsProvider {
    fun prepareAndShowBanner(activity: Activity, container: FrameLayout) {
        container.visibility = View.GONE
    }
}
