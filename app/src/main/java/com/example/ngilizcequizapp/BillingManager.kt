package com.example.ngilizcequizapp

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 🎯 Google Play Billing v7 - Merkezi Ürün ID Tanımlamaları
 * Google Play Console üzerindeki SKU ID'leri ile birebir eşleşir.
 */
object BillingProducts {
    // ------------------------------------------------------------------------
    // 1. ABONELİKLER (SUBS)
    // ------------------------------------------------------------------------
    const val SUB_PREMIUM_MONTHLY = "mew_premium_monthly"
    const val SUB_PREMIUM_PLUS_MONTHLY = "mew_premium_plus_monthly"
    const val SUB_FAMILY_PLAN_MONTHLY = "mew_family_plan_monthly"

    // Geriye dönük uyumluluk takma adları (Aliases)
    const val SUBS_PREMIUM_MONTHLY = SUB_PREMIUM_MONTHLY
    const val SUBS_PREMIUM_PLUS_MONTHLY = SUB_PREMIUM_PLUS_MONTHLY
    const val SUBS_FAMILY_PLAN_MONTHLY = SUB_FAMILY_PLAN_MONTHLY

    // ------------------------------------------------------------------------
    // 2. TÜKETİLEBİLİR ÜRÜNLER (INAPP - Consumable: Elmas ve Altın Paketleri)
    // ------------------------------------------------------------------------
    const val INAPP_GEMS_60 = "gems_60"
    const val INAPP_GEMS_300 = "gems_300"
    const val INAPP_GEMS_900 = "gems_900"
    const val INAPP_COINS_5000 = "coins_5000"
    const val INAPP_COINS_25000 = "coins_25000"

    // ------------------------------------------------------------------------
    // 3. KALICI TEK SEFERLİK ÜRÜNLER (INAPP - Non-Consumable: Temalar ve Kanatlar)
    // ------------------------------------------------------------------------
    const val INAPP_THEME_KINGDOM = "theme_kingdom"
    const val INAPP_THEME_SPACE = "theme_space"

    val ALL_SUBS = listOf(
        SUB_PREMIUM_MONTHLY,
        SUB_PREMIUM_PLUS_MONTHLY,
        SUB_FAMILY_PLAN_MONTHLY
    )

    val ALL_CONSUMABLES = listOf(
        INAPP_GEMS_60,
        INAPP_GEMS_300,
        INAPP_GEMS_900,
        INAPP_COINS_5000,
        INAPP_COINS_25000
    )

    val ALL_NON_CONSUMABLES = listOf(
        INAPP_THEME_KINGDOM,
        INAPP_THEME_SPACE
    )

    val ALL_INAPPS = ALL_CONSUMABLES + ALL_NON_CONSUMABLES

    fun isSubscription(productId: String): Boolean = productId in ALL_SUBS
    fun isConsumable(productId: String): Boolean = productId in ALL_CONSUMABLES
    fun isNonConsumable(productId: String): Boolean = productId in ALL_NON_CONSUMABLES

    fun getProductName(productId: String): String {
        return when (productId) {
            SUB_PREMIUM_MONTHLY -> "MewEnglish Premium"
            SUB_PREMIUM_PLUS_MONTHLY -> "MewEnglish Premium Plus"
            SUB_FAMILY_PLAN_MONTHLY -> "MewEnglish Aile Planı"
            INAPP_GEMS_60 -> "60 Elmas Paketi"
            INAPP_GEMS_300 -> "300 Elmas Paketi"
            INAPP_GEMS_900 -> "900 Elmas Paketi"
            INAPP_COINS_5000 -> "5.000 Altın Paketi"
            INAPP_COINS_25000 -> "25.000 Altın Paketi"
            INAPP_THEME_KINGDOM -> "Krallık Mew Teması & Kanatları"
            INAPP_THEME_SPACE -> "Kozmik Uzay Teması"
            else -> productId
        }
    }
}

/**
 * UI katmanına iletilen satın alma ve faturalandırma olayları
 */
sealed class BillingEvent {
    data class PurchaseSuccess(val productId: String, val message: String = "") : BillingEvent()
    data class PurchaseFailed(val productId: String?, val errorMessage: String) : BillingEvent()
    data class PurchaseError(val message: String) : BillingEvent()
    data class PurchaseCancelled(val productId: String?) : BillingEvent()
    data class PurchasePending(val productId: String?) : BillingEvent()
    data class PurchasesRestored(val restoredCount: Int, val message: String = "") : BillingEvent()
    data class ItemAlreadyOwned(val productId: String) : BillingEvent()
}

/**
 * 💳 Google Play Billing v7 Entegrasyon Yöneticisi
 */
object BillingManager : PurchasesUpdatedListener {

    private const val TAG = "BillingManager"

    private var appContext: Context? = null
    private var billingClient: BillingClient? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring = _isRestoring.asStateFlow()

    private val _productDetailsMap = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetailsMap = _productDetailsMap.asStateFlow()

    private val _events = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 20)
    val events = _events.asSharedFlow()

    var lastPendingProductId: String? by mutableStateOf(null)
        private set

    @Volatile
    private var isConnecting = false
    private var reconnectAttempts = 0

    /**
     * Uygulama başlangıcında (MainActivity vb.) tek seferlik initialize edilir.
     */
    fun init(context: Context) {
        if (appContext != null && billingClient != null) return
        appContext = context.applicationContext

        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()

        billingClient = BillingClient.newBuilder(context.applicationContext)
            .setListener(this)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()

        startConnection()
    }

    /**
     * Google Play Billing bağlantısını başlatır ve koparsa üstel geri çekilmeyle yeniden bağlanır.
     */
    fun startConnection(onConnected: (() -> Unit)? = null) {
        val client = billingClient ?: run {
            appContext?.let { init(it) }
            billingClient ?: return
        }

        if (client.isReady) {
            _isReady.value = true
            queryAllProductDetails()
            onConnected?.invoke()
            return
        }

        if (isConnecting) {
            Log.d(TAG, "BillingClient bağlantı süreci zaten devam ediyor...")
            return
        }

        isConnecting = true

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isConnecting = false
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "✅ BillingClient v7 bağlantısı başarıyla kuruldu.")
                    _isReady.value = true
                    reconnectAttempts = 0
                    queryAllProductDetails()
                    queryPurchasesAsync()
                    onConnected?.invoke()
                } else {
                    Log.w(TAG, "⚠️ BillingClient kurulum hatası: ${billingResult.debugMessage} (Kod: ${billingResult.responseCode})")
                    _isReady.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnecting = false
                _isReady.value = false
                reconnectAttempts++
                val delayTime = (2000L * (1 shl minOf(reconnectAttempts - 1, 3))).coerceAtMost(16000L)
                Log.w(TAG, "⚠️ Google Play servis bağlantısı kesildi. ${delayTime / 1000}s sonra yeniden bağlanılacak...")
                scope.launch {
                    delay(delayTime)
                    startConnection(onConnected)
                }
            }
        })
    }

    /**
     * Konsolda tanımlı tüm abonelik ve tek seferlik ürünlerin detaylarını ve yerel fiyatlarını sorgular.
     */
    fun queryAllProductDetails() {
        val client = billingClient ?: return
        if (!client.isReady) return

        // 1. INAPP Ürünleri Sorgula
        val inappProductList = BillingProducts.ALL_INAPPS.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val inappParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inappProductList)
            .build()

        client.queryProductDetailsAsync(inappParams) { result, productDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val currentMap = _productDetailsMap.value.toMutableMap()
                productDetailsList.forEach { details ->
                    currentMap[details.productId] = details
                    Log.d(TAG, "INAPP Detayı Yüklendi: ${details.productId} -> ${details.oneTimePurchaseOfferDetails?.formattedPrice}")
                }
                _productDetailsMap.value = currentMap
            } else {
                Log.w(TAG, "INAPP sorgusu başarısız: ${result.debugMessage}")
            }
        }

        // 2. SUBS Abonelikleri Sorgula
        val subsProductList = BillingProducts.ALL_SUBS.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val subsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subsProductList)
            .build()

        client.queryProductDetailsAsync(subsParams) { result, productDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val currentMap = _productDetailsMap.value.toMutableMap()
                productDetailsList.forEach { details ->
                    currentMap[details.productId] = details
                    val formattedPrice = details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    Log.d(TAG, "SUBS Detayı Yüklendi: ${details.productId} -> $formattedPrice")
                }
                _productDetailsMap.value = currentMap
            } else {
                Log.w(TAG, "SUBS sorgusu başarısız: ${result.debugMessage}")
            }
        }
    }

    /**
     * Google Play'den gelen gerçek para fiyatını döndürür; henüz yüklenmediyse fallback fiyatını gösterir.
     */
    fun getFormattedPrice(productId: String, fallbackPrice: String): String {
        val details = _productDetailsMap.value[productId] ?: return fallbackPrice
        return when (details.productType) {
            BillingClient.ProductType.INAPP -> {
                details.oneTimePurchaseOfferDetails?.formattedPrice ?: fallbackPrice
            }
            BillingClient.ProductType.SUBS -> {
                details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    ?: fallbackPrice
            }
            else -> fallbackPrice
        }
    }

    /**
     * Kullanıcı bir satın alma butonuna bastığında Google Play Satın Alma Sayfasını açar.
     */
    fun launchBillingFlow(
        activity: Activity,
        productId: String,
        selectedOfferToken: String? = null
    ) {
        val client = billingClient ?: run {
            init(activity.applicationContext)
            billingClient
        }

        if (client == null || !client.isReady) {
            startConnection {
                launchBillingFlow(activity, productId, selectedOfferToken)
            }
            Toast.makeText(activity, "Google Play Mağazasına bağlanılıyor, lütfen tekrar deneyin...", Toast.LENGTH_SHORT).show()
            return
        }

        val productDetails = _productDetailsMap.value[productId]
        if (productDetails == null) {
            Log.w(TAG, "Ürün detayları henüz hazır değil: $productId. Sorgulanıyor...")
            queryAllProductDetails()
            Toast.makeText(activity, "Ürün bilgisi alınıyor, lütfen birkaç saniye sonra tekrar deneyin.", Toast.LENGTH_SHORT).show()
            return
        }

        lastPendingProductId = productId

        val productDetailsParams = when (productDetails.productType) {
            BillingClient.ProductType.SUBS -> {
                val offerToken = selectedOfferToken
                    ?: productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                    ?: run {
                        Log.e(TAG, "Abonelik için offerToken bulunamadı: $productId")
                        Toast.makeText(activity, "Abonelik planı bulunamadı.", Toast.LENGTH_SHORT).show()
                        return
                    }

                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            }
            else -> {
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            }
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val response = client.launchBillingFlow(activity, flowParams)
        when (response.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG, "launchBillingFlow başarıyla başlatıldı.")
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _events.tryEmit(BillingEvent.PurchaseCancelled(productId))
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _events.tryEmit(BillingEvent.ItemAlreadyOwned(productId))
                queryPurchasesAsync()
            }
            else -> {
                Log.e(TAG, "launchBillingFlow başarısız: ${response.debugMessage} (Kod: ${response.responseCode})")
                _events.tryEmit(BillingEvent.PurchaseFailed(productId, response.debugMessage))
            }
        }
    }

    /**
     * Satın alma işlemi tamamlandığında veya iptal edildiğinde Google Play'den çağrılır.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        val context = appContext ?: return
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    for (purchase in purchases) {
                        handlePurchase(context, purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Kullanıcı satın alma ekranını kapattı/iptal etti.")
                _events.tryEmit(BillingEvent.PurchaseCancelled(lastPendingProductId))
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "Öğe zaten satın alınmış, haklar kontrol ediliyor.")
                _events.tryEmit(BillingEvent.ItemAlreadyOwned(lastPendingProductId ?: ""))
                queryPurchasesAsync()
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                Log.w(TAG, "Google Play servis bağlantısı koptu.")
                _events.tryEmit(BillingEvent.PurchaseError("Google Play bağlantısı koptu. Lütfen tekrar deneyin."))
                startConnection()
            }
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.NETWORK_ERROR -> {
                Log.w(TAG, "Ağ veya Google Play servis hatası: ${billingResult.debugMessage}")
                _events.tryEmit(BillingEvent.PurchaseError("İnternet bağlantınızı kontrol edin ve tekrar deneyin."))
            }
            else -> {
                Log.e(TAG, "Satın alma hatası: ${billingResult.debugMessage} (Kod: ${billingResult.responseCode})")
                _events.tryEmit(BillingEvent.PurchaseFailed(lastPendingProductId, billingResult.debugMessage))
            }
        }
    }

    /**
     * Başarılı satın alımı işler, consumable ise tüketir (consume), abonelik/kalıcı ise onaylar (acknowledge).
     */
    private fun handlePurchase(context: Context, purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PENDING -> {
                Log.d(TAG, "Satın alma beklemede (PENDING): ${purchase.orderId}")
                purchase.products.forEach { pid ->
                    _events.tryEmit(BillingEvent.PurchasePending(pid))
                }
                return
            }
            Purchase.PurchaseState.PURCHASED -> {
                val productIds = purchase.products
                for (productId in productIds) {
                    // 1. Tüketilebilir ürünler (Elmas & Altın paketleri)
                    if (BillingProducts.isConsumable(productId)) {
                        val consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()

                        billingClient?.consumeAsync(consumeParams) { result, _ ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                Log.d(TAG, "Ürün tüketildi (Consumable OK): $productId")
                                deliverPurchase(context, productId)
                                _events.tryEmit(BillingEvent.PurchaseSuccess(productId, "${BillingProducts.getProductName(productId)} başarıyla tanımlandı!"))
                            } else {
                                Log.e(TAG, "Consume hatası: ${result.debugMessage}")
                                _events.tryEmit(BillingEvent.PurchaseError("Ürün teslim edilirken bir sorun oluştu: ${result.debugMessage}"))
                            }
                        }
                    }
                    // 2. Kalıcı ürünler ve Abonelikler (Onaylama - Acknowledge zorunlu)
                    else {
                        if (!purchase.isAcknowledged) {
                            val ackParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()

                            billingClient?.acknowledgePurchase(ackParams) { result ->
                                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                    Log.d(TAG, "Ürün onaylandı (Acknowledge OK): $productId")
                                    deliverPurchase(context, productId)
                                    _events.tryEmit(BillingEvent.PurchaseSuccess(productId, "${BillingProducts.getProductName(productId)} başarıyla aktif edildi!"))
                                } else {
                                    Log.e(TAG, "Acknowledge hatası: ${result.debugMessage}")
                                    _events.tryEmit(BillingEvent.PurchaseError("İşlem onaylanamadı: ${result.debugMessage}"))
                                }
                            }
                        } else {
                            deliverPurchase(context, productId)
                        }
                    }
                }
            }
            else -> {
                Log.w(TAG, "Bilinmeyen purchaseState: ${purchase.purchaseState}")
            }
        }
    }

    /**
     * Satın alınan ürünü kullanıcının hesabına (Store, SharedPreferences ve Firestore) işler.
     */
    fun deliverPurchase(context: Context, productId: String) {
        Log.i(TAG, "deliverPurchase çağrıldı -> Product ID: $productId")
        when (productId) {
            BillingProducts.INAPP_GEMS_60 -> {
                QuizTreeProgressStore.addGems(context, 60)
                FirestoreSyncManager.recordPurchase(context, packageName = "60 Elmas", price = "₺29,99")
            }
            BillingProducts.INAPP_GEMS_300 -> {
                QuizTreeProgressStore.addGems(context, 300)
                FirestoreSyncManager.recordPurchase(context, packageName = "300 Elmas", price = "₺129,99")
            }
            BillingProducts.INAPP_GEMS_900 -> {
                QuizTreeProgressStore.addGems(context, 900)
                FirestoreSyncManager.recordPurchase(context, packageName = "900 Elmas", price = "₺349,99")
            }
            BillingProducts.INAPP_COINS_5000 -> {
                QuizTreeProgressStore.addCoins(context, 5000)
                FirestoreSyncManager.recordPurchase(context, packageName = "5.000 Altın", price = "₺59,99")
            }
            BillingProducts.INAPP_COINS_25000 -> {
                QuizTreeProgressStore.addCoins(context, 25000)
                FirestoreSyncManager.recordPurchase(context, packageName = "25.000 Altın", price = "₺239,99")
            }
            BillingProducts.INAPP_THEME_KINGDOM -> {
                UserCosmeticsStore.unlockTheme(context, "kingdom")
                UserCosmeticsStore.setEquippedTheme(context, "kingdom")
                UserCosmeticsStore.unlockWing(context, "kingdom_kanat")
                UserCosmeticsStore.setEquippedWingId(context, "kingdom_kanat")
                FirestoreSyncManager.recordPurchase(context, packageName = "Krallık Mew Teması (+Krallık Kanadı)", price = "₺50,00")
                FirestoreSyncManager.syncProgress(context)
            }
            BillingProducts.INAPP_THEME_SPACE -> {
                UserCosmeticsStore.unlockTheme(context, "uzay")
                UserCosmeticsStore.setEquippedTheme(context, "uzay")
                FirestoreSyncManager.recordPurchase(context, packageName = "Uzay Teması", price = "₺20,00")
                FirestoreSyncManager.syncProgress(context)
            }
            BillingProducts.SUB_PREMIUM_MONTHLY -> {
                UserProfileManager.setPremiumPlan(context, "premium")
                QuizTreeProgressStore.addGems(context, 200)
                QuizTreeProgressStore.addCoins(context, 1000)
                navigation.MarketBoostStore.activateBoost(context, "1 Haftalık 2X XP", navigation.BoostType.XP, 7 * 24 * 60, 2)
                navigation.MarketBoostStore.activateBoost(context, "1 Haftalık 2X Coin", navigation.BoostType.COIN, 7 * 24 * 60, 2)
                FirestoreSyncManager.recordPurchase(
                    context,
                    packageName = "Premium",
                    price = "₺149,00 / ay",
                    isPremiumUpgrade = true,
                    premiumPlanName = "premium"
                )
                FirestoreSyncManager.syncProgress(context)
            }
            BillingProducts.SUB_PREMIUM_PLUS_MONTHLY -> {
                UserProfileManager.setPremiumPlan(context, "premium_plus")
                QuizTreeProgressStore.addGems(context, 300)
                QuizTreeProgressStore.addCoins(context, 2000)
                navigation.MarketBoostStore.activateBoost(context, "1 Haftalık 2X XP", navigation.BoostType.XP, 7 * 24 * 60, 2)
                navigation.MarketBoostStore.activateBoost(context, "1 Haftalık 2X Coin", navigation.BoostType.COIN, 7 * 24 * 60, 2)
                FirestoreSyncManager.recordPurchase(
                    context,
                    packageName = "Premium Plus",
                    price = "₺199,00 / ay",
                    isPremiumUpgrade = true,
                    premiumPlanName = "premium_plus"
                )
                FirestoreSyncManager.syncProgress(context)
            }
            BillingProducts.SUB_FAMILY_PLAN_MONTHLY -> {
                UserProfileManager.setPremiumPlan(context, "family_plan")
                QuizTreeProgressStore.addGems(context, 300)
                QuizTreeProgressStore.addCoins(context, 2000)
                navigation.MarketBoostStore.activateBoost(context, "1 Haftalık 2X XP", navigation.BoostType.XP, 7 * 24 * 60, 2)
                navigation.MarketBoostStore.activateBoost(context, "1 Haftalık 2X Coin", navigation.BoostType.COIN, 7 * 24 * 60, 2)
                FirestoreSyncManager.recordPurchase(
                    context,
                    packageName = "Aile Planı",
                    price = "₺399,00 / ay",
                    isPremiumUpgrade = true,
                    premiumPlanName = "family_plan"
                )
                FirestoreSyncManager.syncProgress(context)
            }
        }
        UserProfileManager.notifyStateChanged()
        UserCosmeticsStore.notifyStateChanged()
    }

    /**
     * 🔄 "Satın Alımları Geri Yükle" (Restore Purchases) Mekanizması
     * Kullanıcının daha önce satın aldığı aktif abonelikleri veya kalıcı ürünleri
     * Google Play üzerinden sorgular, onaylanmamış olanları anında onaylar ve hesaba uygular.
     */
    fun restorePurchases(
        context: Context,
        onResult: ((isSuccess: Boolean, restoredCount: Int, message: String) -> Unit)? = null
    ) {
        val client = billingClient ?: run {
            init(context.applicationContext)
            billingClient
        }

        if (client == null || !client.isReady) {
            _isRestoring.value = true
            startConnection {
                restorePurchases(context, onResult)
            }
            return
        }

        _isRestoring.value = true
        var restoredCount = 0

        // 1. Aktif Abonelikleri (SUBS) Sorgula
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(subsParams) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                var foundActiveSub = false
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        foundActiveSub = true
                        // Eğer henüz onaylanmamışsa Google Play'in iade etmemesi için hemen onayla
                        if (!purchase.isAcknowledged) {
                            val ackParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                            client.acknowledgePurchase(ackParams) { ackRes ->
                                Log.d(TAG, "Geri yükleme sırasında onaylandı: ${ackRes.responseCode}")
                            }
                        }

                        for (productId in purchase.products) {
                            when (productId) {
                                BillingProducts.SUB_PREMIUM_MONTHLY -> {
                                    UserProfileManager.setPremiumPlan(context, "premium")
                                    restoredCount++
                                }
                                BillingProducts.SUB_PREMIUM_PLUS_MONTHLY -> {
                                    UserProfileManager.setPremiumPlan(context, "premium_plus")
                                    restoredCount++
                                }
                                BillingProducts.SUB_FAMILY_PLAN_MONTHLY -> {
                                    UserProfileManager.setPremiumPlan(context, "family_plan")
                                    restoredCount++
                                }
                            }
                        }
                    }
                }

                if (!foundActiveSub && UserProfileManager.isPremium(context)) {
                    Log.d(TAG, "Aktif Google Play aboneliği bulunamadı, plan freemium olarak korundu.")
                }
            } else {
                Log.w(TAG, "Abonelik sorgusu başarısız: ${result.debugMessage}")
            }

            // 2. Kalıcı INAPP Ürünleri Sorgula
            val inappParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            client.queryPurchasesAsync(inappParams) { inappResult, inappPurchases ->
                _isRestoring.value = false

                if (inappResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in inappPurchases) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            if (!purchase.isAcknowledged) {
                                val ackParams = AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()
                                client.acknowledgePurchase(ackParams) { ackRes ->
                                    Log.d(TAG, "Kalıcı ürün geri yükleme sırasında onaylandı: ${ackRes.responseCode}")
                                }
                            }

                            for (productId in purchase.products) {
                                when (productId) {
                                    BillingProducts.INAPP_THEME_KINGDOM -> {
                                        UserCosmeticsStore.unlockTheme(context, "kingdom")
                                        UserCosmeticsStore.unlockWing(context, "kingdom_kanat")
                                        restoredCount++
                                    }
                                    BillingProducts.INAPP_THEME_SPACE -> {
                                        UserCosmeticsStore.unlockTheme(context, "uzay")
                                        restoredCount++
                                    }
                                }
                            }
                        }
                    }
                }

                UserProfileManager.notifyStateChanged()
                UserCosmeticsStore.notifyStateChanged()
                FirestoreSyncManager.syncProgress(context)

                val message = if (restoredCount > 0) {
                    "Tebrikler! $restoredCount adet satın alım ve aboneliğiniz geri yüklendi."
                } else {
                    "Aktif bir satın alım veya abonelik kaydı bulunamadı."
                }

                _events.tryEmit(BillingEvent.PurchasesRestored(restoredCount, message))
                onResult?.invoke(true, restoredCount, message)
            }
        }
    }

    /**
     * Uygulama açılışında veya arka plandan dönüşte senkronizasyon için kullanılır.
     */
    fun queryPurchasesAsync(onComplete: ((Int) -> Unit)? = null) {
        val context = appContext ?: return
        restorePurchases(context) { _, count, _ ->
            onComplete?.invoke(count)
        }
    }
}

