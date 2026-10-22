package com.example.ngilizcequizapp

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirestoreSyncManager {
    private const val TAG = "FirestoreSyncManager"
    private const val COLLECTION_USERS = "user_profiles"
    private const val DATABASE_NAME = "mewenglish"

    private var snapshotListener: ListenerRegistration? = null
    private var isUpdatingFromCloud = false

    /**
     * MewPanel admin paneli 'mewenglish' adlı özel veritabanını kullanmaktadır.
     * Bu fonksiyon 'mewenglish' veritabanına bağlanır.
     */
    fun getFirestoreDb(): FirebaseFirestore {
        return try {
            FirebaseFirestore.getInstance(FirebaseApp.getInstance(), DATABASE_NAME)
        } catch (e: Exception) {
            Log.w(TAG, "'$DATABASE_NAME' veritabanına bağlanılamadı, varsayılan deneniyor: ${e.message}")
            FirebaseFirestore.getInstance()
        }
    }

    private fun getCurrentIsoTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
    }

    /**
     * Uygulama açılışında çağrılır:
     * Lazy Auth ile arka planda signInAnonymously() çalışır ve
     * no_name_user_{artan_sayac} profili admin paneline kaydedilir.
     */
    fun init(context: Context) {
        initLazyAnonymousAuth(context)
    }

    /**
     * Açılış & Lazy Auth Mantığı:
     * - Uygulama ilk açılışta KESİNLİKLE giriş/kayıt ekranı göstermez.
     * - Arka planda Firebase signInAnonymously() çalışır.
     * - Admin/Firestore panelinde bu kullanıcıların görünen adı: no_name_user_{artan_sayac}
     *   (mevcut no_name_user sayısına göre sıralı numara atanır).
     */
    fun initLazyAnonymousAuth(context: Context, onReady: (() -> Unit)? = null) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val localName = UserProfileManager.getUserFirstName(context)
        val isRegistered = UserProfileManager.isRegistered(context)

        // Eğer kullanıcı zaten gerçek bir hesaba bağlıysa
        if (currentUser != null && !currentUser.isAnonymous && UserProfileManager.getUserEmail(context).isNotBlank()) {
            startRealtimeListener(context)
            syncProgress(context)
            onReady?.invoke()
            return
        }

        // Eğer daha önce no_name_user_ atanmışsa ve yerelde varsa
        if (localName.startsWith("no_name_user_") && isRegistered) {
            if (currentUser == null) {
                auth.signInAnonymously().addOnCompleteListener {
                    startRealtimeListener(context)
                    syncProgress(context)
                    onReady?.invoke()
                }
            } else {
                startRealtimeListener(context)
                syncProgress(context)
                onReady?.invoke()
            }
            return
        }

        // İlk kez anonim kullanıcı oluşturuluyor
        fun assignNoNameUser(uid: String) {
            val db = getFirestoreDb()
            db.collection(COLLECTION_USERS)
                .whereGreaterThanOrEqualTo("name", "no_name_user_")
                .whereLessThanOrEqualTo("name", "no_name_user_\uf8ff")
                .get()
                .addOnSuccessListener { snapshots ->
                    val existingCount = snapshots?.size() ?: 0
                    val assignedName = "no_name_user_${existingCount + 1}"
                    Log.d(TAG, "Yeni anonim kullanıcı oluşturuldu: $assignedName (uid: $uid)")

                    // Yeni kullanıcı sıfır durumu (Zero-State: 0 Coin, 0 Elmas, 0 XP, Bronz Rank)
                    UserProfileManager.initializeZeroState(context)

                    UserProfileManager.saveLocalProfile(
                        context = context,
                        name = assignedName,
                        surname = "",
                        email = "",
                        age = 18,
                        premiumPlan = "freemium"
                    )
                    UserProfileManager.setRegisteredFlag(context, true)

                    pushInitialProfile(
                        context = context,
                        userId = uid,
                        name = assignedName,
                        surname = "",
                        email = "",
                        age = 18
                    ) { _, _ ->
                        startRealtimeListener(context)
                        onReady?.invoke()
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Mevcut no_name_user sayısı alınamadı: ${e.message}")
                    val assignedName = "no_name_user_1"
                    UserProfileManager.initializeZeroState(context)
                    UserProfileManager.saveLocalProfile(
                        context = context,
                        name = assignedName,
                        surname = "",
                        email = "",
                        age = 18,
                        premiumPlan = "freemium"
                    )
                    UserProfileManager.setRegisteredFlag(context, true)
                    pushInitialProfile(
                        context = context,
                        userId = uid,
                        name = assignedName,
                        surname = "",
                        email = "",
                        age = 18
                    ) { _, _ ->
                        startRealtimeListener(context)
                        onReady?.invoke()
                    }
                }
        }

        if (currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: UserProfileManager.getUserId(context)
                    assignNoNameUser(uid)
                }
                .addOnFailureListener { err ->
                    Log.e(TAG, "Anonim giriş başarısız: ${err.message}")
                    assignNoNameUser(UserProfileManager.getUserId(context))
                }
        } else {
            assignNoNameUser(currentUser.uid)
        }
    }

    /**
     * Firebase Firestore'dan canlı dinleyici başlatır (addSnapshotListener).
     * Admin panelinde (MewPanel) yapılan değişiklikler (Coin, Elmas, XP, Seri, İsim, Soyisim, vb.)
     * anında kullanıcının cihazına yansır ve UI yeniden çizilir.
     */
    fun startRealtimeListener(context: Context) {
        if (snapshotListener != null) return

        val userId = UserProfileManager.getUserId(context)
        val db = getFirestoreDb()

        Log.d(TAG, "Canlı Firestore dinleyicisi başlatılıyor... UserId: $userId ($DATABASE_NAME db)")

        snapshotListener = db.collection(COLLECTION_USERS)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore dinleyici hatası: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        isUpdatingFromCloud = true

                        // 1. İsim / Soyisim / E-posta / Yaş / Plan
                        val cloudName = snapshot.getString("name")
                        val cloudSurname = snapshot.getString("surname")
                        val cloudEmail = snapshot.getString("email")
                        val cloudAge = snapshot.getLong("age")?.toInt()
                        val cloudPlan = snapshot.getString("premium_plan")

                        if (!cloudName.isNullOrBlank() || !cloudSurname.isNullOrBlank()) {
                            UserProfileManager.saveLocalProfile(
                                context = context,
                                name = cloudName ?: UserProfileManager.getUserFirstName(context),
                                surname = cloudSurname ?: UserProfileManager.getUserLastName(context),
                                email = cloudEmail ?: UserProfileManager.getUserEmail(context),
                                age = cloudAge ?: UserProfileManager.getUserAge(context),
                                premiumPlan = cloudPlan
                            )
                        }

                        // 2. Coin, Elmas (Gems) ve XP
                        val cloudCoin = snapshot.getLong("coin")?.toInt()
                        val cloudElmas = snapshot.getLong("elmas")?.toInt()
                        val cloudXp = snapshot.getLong("xp")?.toInt()

                        var statsChanged = false
                        if (cloudCoin != null && cloudCoin != QuizTreeProgressStore.getCoins(context)) {
                            QuizTreeProgressStore.setCoins(context, cloudCoin)
                            statsChanged = true
                        }
                        if (cloudElmas != null && cloudElmas != QuizTreeProgressStore.getGems(context)) {
                            QuizTreeProgressStore.setGems(context, cloudElmas)
                            statsChanged = true
                        }
                        if (cloudXp != null && cloudXp != QuizTreeProgressStore.getXp(context)) {
                            QuizTreeProgressStore.setXp(context, cloudXp)
                            statsChanged = true
                        }

                        // 3. Streak (Seri)
                        val cloudStreak = snapshot.getLong("streak")?.toInt()
                        if (cloudStreak != null && cloudStreak != StreakManager.getStreak(context)) {
                            StreakManager.setStreak(context, cloudStreak)
                            statsChanged = true
                        }

                        // State bildirimleri (UI Recomposition tetikle)
                        UserProfileManager.notifyStateChanged()
                        UserCosmeticsStore.notifyStateChanged()

                        Log.d(TAG, "Admin panelinden gelen değişiklikler uygulandı. Coin: $cloudCoin, Elmas: $cloudElmas, XP: $cloudXp, Streak: $cloudStreak")
                    } catch (e: Exception) {
                        Log.e(TAG, "Cloud güncellemesi işlenirken hata: ${e.message}", e)
                    } finally {
                        isUpdatingFromCloud = false
                    }
                }
            }
    }

    /**
     * Google ile girişte var olan profili kurtarır veya yeni profil oluşturur.
     */
    fun checkAndRestoreOrSaveProfile(
        context: Context,
        userId: String,
        name: String,
        surname: String,
        email: String,
        age: Int,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val db = getFirestoreDb()
        val docRef = db.collection(COLLECTION_USERS).document(userId)

        docRef.get().addOnSuccessListener { snapshot ->
            if (snapshot != null && snapshot.exists()) {
                // Kullanıcı daha önce Google ile giriş yapmış, verilerini yerel hafızaya yükle!
                try {
                    val restoredName = snapshot.getString("name") ?: name
                    val restoredSurname = snapshot.getString("surname") ?: surname
                    val restoredEmail = snapshot.getString("email") ?: email
                    val restoredAge = snapshot.getLong("age")?.toInt() ?: age
                    val restoredPlan = snapshot.getString("premium_plan") ?: "freemium"
                    val restoredCoin = snapshot.getLong("coin")?.toInt() ?: 0
                    val restoredElmas = snapshot.getLong("elmas")?.toInt() ?: 0
                    val restoredXp = snapshot.getLong("xp")?.toInt() ?: 0
                    val restoredStreak = snapshot.getLong("streak")?.toInt() ?: 1

                    UserProfileManager.saveLocalProfile(
                        context = context,
                        name = restoredName,
                        surname = restoredSurname,
                        email = restoredEmail,
                        age = restoredAge,
                        premiumPlan = restoredPlan
                    )
                    QuizTreeProgressStore.setCoins(context, restoredCoin)
                    QuizTreeProgressStore.setGems(context, restoredElmas)
                    QuizTreeProgressStore.setXp(context, restoredXp)
                    StreakManager.setStreak(context, restoredStreak)

                    // updated_at ve age güncelle
                    val updates = hashMapOf<String, Any>(
                        "updated_at" to getCurrentIsoTimestamp()
                    )
                    if (age > 0) updates["age"] = age
                    docRef.update(updates)

                    Log.d(TAG, "Mevcut kullanıcı verileri Firestore'dan başarıyla geri yüklendi.")
                    onComplete(true, null)
                } catch (e: Exception) {
                    Log.e(TAG, "Veri geri yükleme hatası: ${e.message}", e)
                    onComplete(true, null)
                }
            } else {
                // Yeni kullanıcı, ilk kaydı oluştur (Zero-State: 0 Coin, 0 Elmas, 0 XP, Bronz Rank)
                UserProfileManager.initializeZeroState(context)
                pushInitialProfile(
                    context = context,
                    userId = userId,
                    name = name,
                    surname = surname,
                    email = email,
                    age = age,
                    onComplete = onComplete
                )
            }
        }.addOnFailureListener { e ->
            Log.w(TAG, "Kullanıcı sorgulanamadı, yeni profil yazılıyor: ${e.message}")
            UserProfileManager.initializeZeroState(context)
            pushInitialProfile(
                context = context,
                userId = userId,
                name = name,
                surname = surname,
                email = email,
                age = age,
                onComplete = onComplete
            )
        }
    }

    /**
     * "Zaten Hesabım Var" seçildiğinde e-posta adresi ile Firestore 'user_profiles' koleksiyonundan
     * kullanıcının verilerini (Coin, Elmas, XP, Streak, Plan, İsim vb.) admin panelinden çeker ve sisteme yükler.
     */
    fun restoreAccountByEmail(
        context: Context,
        email: String,
        enteredName: String = "",
        enteredSurname: String = "",
        enteredAge: Int = 0,
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanEmail = email.trim()
        val db = getFirestoreDb()

        Log.d(TAG, "Hesap kurtarma/giriş başlatılıyor: $cleanEmail")

        // 1. Önce e-posta ile ara
        db.collection(COLLECTION_USERS)
            .whereEqualTo("email", cleanEmail)
            .get()
            .addOnSuccessListener { snapshots ->
                val doc = snapshots?.documents?.firstOrNull()
                if (doc != null && doc.exists()) {
                    applyRestoredDocument(context, doc, cleanEmail, enteredName, enteredSurname, enteredAge, onResult)
                } else {
                    // Küçük harf e-posta varyantını dene
                    db.collection(COLLECTION_USERS)
                        .whereEqualTo("email", cleanEmail.lowercase())
                        .get()
                        .addOnSuccessListener { subSnapshots ->
                            val subDoc = subSnapshots?.documents?.firstOrNull()
                            if (subDoc != null && subDoc.exists()) {
                                applyRestoredDocument(context, subDoc, cleanEmail, enteredName, enteredSurname, enteredAge, onResult)
                            } else {
                                // Admin panelinde henüz kayıt yoksa bile sadece mail ile hesabı oluştur ve aç!
                                val defaultName = if (enteredName.isNotBlank()) {
                                    enteredName.trim()
                                } else {
                                    cleanEmail.substringBefore("@")
                                        .replace(".", " ")
                                        .replace("_", " ")
                                        .split(" ")
                                        .filter { it.isNotBlank() }
                                        .joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercase() } }
                                        .ifBlank { "Mew" }
                                }
                                val defaultSurname = enteredSurname.trim()
                                val defaultAge = if (enteredAge in 5..120) enteredAge else 18
                                val newUserId = UserProfileManager.getUserId(context)
                                UserProfileManager.initializeZeroState(context)
                                UserProfileManager.saveRestoredProfile(
                                    context = context,
                                    userId = newUserId,
                                    name = defaultName,
                                    surname = defaultSurname,
                                    email = cleanEmail,
                                    age = defaultAge,
                                    premiumPlan = "freemium"
                                )
                                pushInitialProfile(
                                    context = context,
                                    userId = newUserId,
                                    name = defaultName,
                                    surname = defaultSurname,
                                    email = cleanEmail,
                                    age = defaultAge
                                ) { _, _ ->
                                    startRealtimeListener(context)
                                    onResult(true, "Hesabınız başarıyla kaydedildi ve açıldı! 🐾")
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Hesap arama hatası (lowercase): ${e.message}", e)
                            val defaultName = cleanEmail.substringBefore("@").ifBlank { "Mew" }
                            val newUserId = UserProfileManager.getUserId(context)
                            UserProfileManager.saveRestoredProfile(
                                context = context,
                                userId = newUserId,
                                name = defaultName,
                                surname = "",
                                email = cleanEmail,
                                age = 18,
                                premiumPlan = "freemium"
                            )
                            startRealtimeListener(context)
                            onResult(true, "Hesabınız açıldı! 🐾")
                        }
                }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Hesap arama hatası: ${error.message}", error)
                onResult(false, "Veritabanına ulaşılamadı: ${error.message}")
            }
    }

    private fun applyRestoredDocument(
        context: Context,
        doc: com.google.firebase.firestore.DocumentSnapshot,
        cleanEmail: String,
        enteredName: String,
        enteredSurname: String,
        enteredAge: Int,
        onResult: (Boolean, String) -> Unit
    ) {
        try {
            val foundId = doc.getString("id") ?: doc.id
            val foundName = doc.getString("name")?.ifBlank { null } ?: enteredName.ifBlank { "Mew" }
            val foundSurname = doc.getString("surname")?.ifBlank { null } ?: enteredSurname
            val foundAge = doc.getLong("age")?.toInt() ?: (if (enteredAge in 5..120) enteredAge else 18)
            val foundPlan = doc.getString("premium_plan") ?: "freemium"
            val foundCoin = doc.getLong("coin")?.toInt() ?: 0
            val foundElmas = doc.getLong("elmas")?.toInt() ?: 0
            val foundXp = doc.getLong("xp")?.toInt() ?: 0
            val foundStreak = doc.getLong("streak")?.toInt() ?: 1

            UserProfileManager.saveRestoredProfile(
                context = context,
                userId = foundId,
                name = foundName,
                surname = foundSurname,
                email = cleanEmail,
                age = foundAge,
                premiumPlan = foundPlan
            )
            QuizTreeProgressStore.setCoins(context, foundCoin)
            QuizTreeProgressStore.setGems(context, foundElmas)
            QuizTreeProgressStore.setXp(context, foundXp)
            StreakManager.setStreak(context, foundStreak)

            UserProfileManager.notifyStateChanged()
            UserCosmeticsStore.notifyStateChanged()

            startRealtimeListener(context)
            Log.d(TAG, "Hesap admin panelinden başarıyla geri yüklendi: $foundId ($foundName, Coin: $foundCoin, XP: $foundXp)")
            onResult(true, "Hoş geldin $foundName! Tüm verilerin admin panelinden yüklendi 🐾")
        } catch (e: Exception) {
            Log.e(TAG, "Profil ayrıştırma hatası: ${e.message}", e)
            onResult(false, "Veri okunurken hata oluştu: ${e.message}")
        }
    }

    /**
     * Yeni kullanıcı ilk kez kayıt olduğunda Firestore 'user_profiles' koleksiyonuna ilk profilini yazar.
     */
    fun pushInitialProfile(
        context: Context,
        userId: String,
        name: String,
        surname: String,
        email: String,
        age: Int,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val db = getFirestoreDb()
        val nowIso = getCurrentIsoTimestamp()
        val effectiveRank = if (QuizTreeProgressStore.getXp(context) == 0) "Bronz" else UserCosmeticsStore.getUserEffectiveRank(context).nameStr

        val profileData = hashMapOf<String, Any>(
            "id" to userId,
            "name" to name,
            "surname" to surname,
            "email" to email,
            "age" to age,
            "premium_plan" to "freemium",
            "rank" to effectiveRank,
            "xp" to QuizTreeProgressStore.getXp(context),
            "coin" to QuizTreeProgressStore.getCoins(context),
            "elmas" to QuizTreeProgressStore.getGems(context),
            "streak" to StreakManager.getStreak(context),
            "total_words_saved" to WordStorage.savedWords.size,
            "total_quizzes_solved" to QuizTreeProgressStore.loadAll(context).size,
            "total_readings_completed" to 0,
            "readings_1_star" to 0,
            "readings_2_stars" to 0,
            "readings_3_stars" to 0,
            "purchase_history" to "[]",
            "created_at" to nowIso,
            "updated_at" to nowIso
        )

        db.collection(COLLECTION_USERS)
            .document(userId)
            .set(profileData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Kullanıcı profili Firestore 'mewenglish' veritabanına başarıyla kaydedildi: $userId ($email)")
                onComplete(true, null)
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Kullanıcı profili Firestore'a kaydedilemedi: ${error.message}", error)
                onComplete(false, error.message)
            }
    }

    /**
     * Kullanıcının uygulama içindeki ilerlemesini (XP, Coin, Elmas, Quiz Sayısı vb.) Firestore'a senkronize eder.
     */
    fun syncProgress(context: Context) {
        if (isUpdatingFromCloud || !UserProfileManager.isRegistered(context)) return

        val userId = UserProfileManager.getUserId(context)
        val email = UserProfileManager.getUserEmail(context)
        val db = getFirestoreDb()
        val effectiveRank = UserCosmeticsStore.getUserEffectiveRank(context).nameStr

        val updates = hashMapOf<String, Any>(
            "xp" to QuizTreeProgressStore.getXp(context),
            "coin" to QuizTreeProgressStore.getCoins(context),
            "elmas" to QuizTreeProgressStore.getGems(context),
            "streak" to StreakManager.getStreak(context),
            "rank" to effectiveRank,
            "total_quizzes_solved" to QuizTreeProgressStore.loadAll(context).size,
            "total_words_saved" to WordStorage.savedWords.size,
            "updated_at" to getCurrentIsoTimestamp()
        )
        if (email.isNotBlank()) {
            updates["email"] = email
        }

        db.collection(COLLECTION_USERS)
            .document(userId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "İlerleme Firestore ile senkronize edildi.")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "İlerleme Firestore'a aktarılamadı: ${error.message}")
            }
    }

    /**
     * Marketten yapılan satın alımları yerel prefs ve Firestore'daki purchase_history listesine işler.
     */
    fun recordPurchase(
        context: Context,
        packageName: String,
        price: String,
        isPremiumUpgrade: Boolean = false,
        premiumPlanName: String? = null
    ) {
        val sharedPref = context.getSharedPreferences("mew_user_prefs", Context.MODE_PRIVATE)
        val currentHistory = sharedPref.getStringSet("local_purchase_history", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val entry = "$packageName ($price)"
        currentHistory.add(entry)
        val editor = sharedPref.edit().putStringSet("local_purchase_history", currentHistory)
        if (isPremiumUpgrade && premiumPlanName != null) {
            editor.putString("premium_plan", premiumPlanName)
        }
        editor.apply()

        UserProfileManager.notifyStateChanged()

        if (!UserProfileManager.isRegistered(context)) return

        val userId = UserProfileManager.getUserId(context)
        val db = getFirestoreDb()

        val userDoc = db.collection(COLLECTION_USERS).document(userId)
        userDoc.get().addOnSuccessListener { snapshot ->
            val existingHistory = try {
                val raw = snapshot.getString("purchase_history") ?: "[]"
                JSONArray(raw)
            } catch (e: Exception) {
                JSONArray()
            }

            val newSaleItem = JSONObject().apply {
                put("packageName", packageName)
                put("price", price)
                put("date", getCurrentIsoTimestamp())
            }
            existingHistory.put(newSaleItem)

            val updates = hashMapOf<String, Any>(
                "purchase_history" to existingHistory.toString(),
                "coin" to QuizTreeProgressStore.getCoins(context),
                "elmas" to QuizTreeProgressStore.getGems(context),
                "updated_at" to getCurrentIsoTimestamp()
            )
            if (isPremiumUpgrade && premiumPlanName != null) {
                updates["premium_plan"] = premiumPlanName.lowercase()
            }

            userDoc.set(updates, SetOptions.merge())
        }
    }

    fun syncSavedWords(context: Context, savedWords: List<SavedWord>) {
        if (!UserProfileManager.isRegistered(context)) return
        val userId = UserProfileManager.getUserId(context)
        getFirestoreDb().collection(COLLECTION_USERS)
            .document(userId)
            .set(
                hashMapOf(
                    "total_words_saved" to savedWords.size,
                    "updated_at" to getCurrentIsoTimestamp()
                ),
                SetOptions.merge()
            )
    }
}
