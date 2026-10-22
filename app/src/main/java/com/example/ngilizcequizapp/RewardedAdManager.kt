package com.example.ngilizcequizapp

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * MewEnglish Ödüllü Reklam (Rewarded Ad) Yöneticisi
 * - Kullanıcının enerjisi bittiğinde veya enerji kazanmak istediğinde video reklam izleterek +1 enerji verir.
 * - Test ve Canlı reklam birimlerini güvenle yönetir.
 * - Reklam tamamlandığında otomatik olarak bir sonraki reklamı arka planda önceden yükler (Preload).
 */
object RewardedAdManager {
    private const val TAG = "RewardedAdManager"

    // 🧪 Google Resmi Test Ödüllü Reklam Birimi ID'si
    const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // 🚀 MewEnglish Canlı Ödüllü Reklam Birimi ID'si (AdMob Paneli)
    const val PROD_AD_UNIT_ID = "ca-app-pub-4345848419606468/6016148896"

    /**
     * Test Modu Durumu:
     * - Debug derlemelerde otomatik olarak true olur ve ban riskini önler.
     * - İstenirse manuel olarak da değiştirilebilir.
     */
    var isTestMode: Boolean = BuildConfig.DEBUG

    /**
     * Aktif kullanılan Reklam Birimi ID'si
     */
    val currentAdUnitId: String
        get() = if (isTestMode) TEST_AD_UNIT_ID else PROD_AD_UNIT_ID

    private var rewardedAd: RewardedAd? = null
    private var isAdLoadingInternal = false

    // Jetpack Compose reaktif UI durumları
    var isAdLoaded by mutableStateOf(false)
        private set
    var isLoadingAd by mutableStateOf(false)
        private set

    /**
     * Google Mobile Ads SDK'sını başlatır ve ilk reklamı arka planda yükler.
     * MainActivity.onCreate içinde çağrılır.
     */
    fun init(context: Context) {
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "MobileAds SDK initialized: $status")
                // İlk ödüllü reklamı arka planda hemen yükle
                loadRewardedAd(context.applicationContext)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob initialize error: ${e.localizedMessage}", e)
        }
    }

    /**
     * Ödüllü reklamı arka planda yükler.
     * Halihazırda yüklüyse veya yükleniyorsa mükerrer istek atmaz.
     */
    fun loadRewardedAd(
        context: Context,
        onLoaded: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (rewardedAd != null) {
            isAdLoaded = true
            isLoadingAd = false
            onLoaded?.invoke()
            return
        }

        if (isAdLoadingInternal) {
            Log.d(TAG, "Reklam zaten yükleniyor, yeni istek engellendi.")
            return
        }

        isAdLoadingInternal = true
        isLoadingAd = true
        val adRequest = AdRequest.Builder().build()
        val adUnitId = currentAdUnitId

        Log.d(TAG, "Ödüllü reklam yükleniyor... ID: $adUnitId (TestMode: $isTestMode)")

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ödüllü reklam başarıyla yüklendi! 🎬")
                    rewardedAd = ad
                    isAdLoadingInternal = false
                    isLoadingAd = false
                    isAdLoaded = true
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Ödüllü reklam yüklenemedi: ${error.message} (Hata Kodu: ${error.code})")
                    rewardedAd = null
                    isAdLoadingInternal = false
                    isLoadingAd = false
                    isAdLoaded = false
                    onError?.invoke(error.message)
                }
            }
        )
    }

    /**
     * Kullanıcıya ödüllü reklamı tam ekran gösterir.
     *
     * @param activity Reklamın gösterileceği Activity referansı
     * @param onRewardEarned Kullanıcı videoyu başarıyla izlediğinde tetiklenir (Varsayılan: +1 Enerji ekler)
     * @param onAdDismissed Reklam kapatıldığında tetiklenir
     * @param onAdFailedToShow Reklam açılamazsa veya henüz yüklenmediyse tetiklenir
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (rewardAmount: Int) -> Unit = { amount ->
            EnergyManager.addEnergy(activity, amount)
        },
        onAdDismissed: (() -> Unit)? = null,
        onAdFailedToShow: ((errorMessage: String) -> Unit)? = null
    ) {
        val currentAd = rewardedAd

        if (currentAd == null) {
            val msg = "Reklam henüz yüklenmedi. Arka planda yükleme başlatıldı, lütfen birkaç saniye sonra tekrar deneyin."
            Log.w(TAG, msg)
            onAdFailedToShow?.invoke(msg)
            loadRewardedAd(activity.applicationContext)
            return
        }

        var rewardGranted = false
        var earnedAmount = 1

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ödüllü reklam ekranda gösterildi.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ödüllü reklam gösterilemedi: ${adError.message} (Kod: ${adError.code})")
                rewardedAd = null
                isAdLoaded = false
                onAdFailedToShow?.invoke(adError.message)
                // Sonraki kullanım için yeni reklam yükle
                loadRewardedAd(activity.applicationContext)
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ödüllü reklam kapatıldı.")
                rewardedAd = null
                isAdLoaded = false

                // Eğer kullanıcı videoyu sonuna kadar izlediyse ödülü ver
                if (rewardGranted) {
                    onRewardEarned(earnedAmount)
                }

                onAdDismissed?.invoke()

                // Bir sonraki izleme için arka planda yeni reklamı önceden hazırla
                loadRewardedAd(activity.applicationContext)
            }
        }

        currentAd.show(activity) { rewardItem ->
            Log.d(TAG, "Kullanıcı ödülü hak etti: ${rewardItem.type} - Miktar: ${rewardItem.amount}")
            rewardGranted = true
            earnedAmount = if (rewardItem.amount > 0) rewardItem.amount else 1
        }
    }
}

/**
 * Compose Context içerisinden güvenli Activity referansı alma uzantısı.
 */
fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
