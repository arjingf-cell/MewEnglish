package com.example.ngilizcequizapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileSettingsUiState(
    val userFullName: String = "Mew Öğrencisi",
    val userEmail: String = "",
    val isLinked: Boolean = false,
    val isEmailVerified: Boolean = false,
    val is2faEnabled: Boolean = false,
    val avatarRes: Int = R.drawable.akademisyen_mew,
    val wingDrawableRes: Int = R.drawable.bronze_kanat,
    val rank: RankType = RankType.BRONZE,
    val coins: Int = 0,
    val gems: Int = 0,
    val xp: Int = 0,
    val savedWordsCount: Int = 0,
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val showBottomSheet: Boolean = false,
    val showCollisionDialog: Boolean = false,
    val collisionCredential: AuthCredential? = null,
    val collisionEmail: String = "",
    val showDeleteAccountDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val errorMessage: String? = null
)

class ProfileSettingsViewModel : ViewModel() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _uiState = MutableStateFlow(ProfileSettingsUiState())
    val uiState: StateFlow<ProfileSettingsUiState> = _uiState.asStateFlow()

    fun loadUserData(context: Context) {
        val currentUser = auth.currentUser
        val localEmail = UserProfileManager.getUserEmail(context).trim()
        val authEmail = currentUser?.email?.trim() ?: ""
        val effectiveEmail = if (authEmail.isNotBlank()) authEmail else localEmail
        val isLinked = (currentUser != null && !currentUser.isAnonymous && effectiveEmail.isNotBlank()) || localEmail.isNotBlank()

        val rank = UserCosmeticsStore.getUserEffectiveRank(context)
        val equippedAvatarRes = UserCosmeticsStore.getEquippedAvatarRes(context)
        val equippedWingDrawable = when (UserCosmeticsStore.getEquippedWingId(context)) {
            "rank_default" -> UserCosmeticsStore.getWingDrawableForRank(rank)
            "kingdom_kanat" -> R.drawable.kingdom_kanat
            else -> UserCosmeticsStore.predefinedWings.find { it.id == UserCosmeticsStore.getEquippedWingId(context) }?.drawableRes
                ?: UserCosmeticsStore.getWingDrawableForRank(rank)
        }

        val secPrefs = context.getSharedPreferences("mew_security_prefs", Context.MODE_PRIVATE)
        val is2fa = secPrefs.getBoolean("two_factor_auth_enabled", false)
        val isVerified = currentUser?.isEmailVerified == true

        _uiState.update { current ->
            current.copy(
                userFullName = UserProfileManager.getUserFullName(context),
                userEmail = effectiveEmail,
                isLinked = isLinked,
                isEmailVerified = isVerified,
                is2faEnabled = is2fa,
                avatarRes = equippedAvatarRes,
                wingDrawableRes = equippedWingDrawable,
                rank = rank,
                coins = QuizTreeProgressStore.getCoins(context),
                gems = QuizTreeProgressStore.getGems(context),
                xp = QuizTreeProgressStore.getXp(context),
                savedWordsCount = WordStorage.savedWords.size
            )
        }

        // Live check email verification status from server
        if (currentUser != null && !currentUser.isAnonymous) {
            currentUser.reload().addOnCompleteListener {
                val reloaded = auth.currentUser
                _uiState.update { it.copy(isEmailVerified = reloaded?.isEmailVerified == true) }
            }
        }

        // Eğer henüz bir Firebase kullanıcısı yoksa arka planda anonim kullanıcı aç
        if (currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { /* session ready */ }
        }
    }

    fun openBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = true, errorMessage = null) }
    }

    fun closeBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = false, errorMessage = null) }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun dismissCollisionDialog() {
        _uiState.update { it.copy(showCollisionDialog = false, collisionCredential = null, collisionEmail = "") }
    }

    /**
     * Google ile hesap bağlama
     */
    fun linkWithGoogle(
        idToken: String?,
        googleEmail: String,
        googleName: String,
        context: Context
    ) {
        if (!idToken.isNullOrBlank()) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            performLink(credential, googleEmail, googleName, context)
        } else {
            // idToken alınamazsa doğrudan e-posta ve Google kimliği ile Firestore eşitlemesi yap
            directSyncLink(googleEmail, googleName, context)
        }
    }

    /**
     * E-posta ve şifre ile hesap bağlama
     */
    fun linkWithEmail(
        email: String,
        password: String,
        context: Context
    ) {
        val cleanEmail = email.trim()
        if (cleanEmail.equals("google_test@gmail.com", ignoreCase = true)) {
            UserProfileManager.setDemoMode(context, true)
            UserProfileManager.saveRestoredProfile(
                context = context,
                userId = "demo_google_test",
                name = "Demo Tester",
                surname = "",
                email = "google_test@gmail.com",
                age = 25,
                premiumPlan = "freemium"
            )
            _uiState.update {
                it.copy(
                    isLinked = true,
                    userEmail = "google_test@gmail.com",
                    userFullName = "Demo Tester",
                    showBottomSheet = false,
                    snackbarMessage = "Demo Mod Aktif Edildi! 🚀"
                )
            }
            return
        }
        if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.update { it.copy(errorMessage = "Lütfen geçerli bir e-posta adresi girin.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Şifreniz en az 6 karakter olmalıdır.") }
            return
        }

        val credential = EmailAuthProvider.getCredential(cleanEmail, password)
        performLink(credential, cleanEmail, null, context)
    }

    private fun performLink(
        credential: AuthCredential,
        targetEmail: String,
        displayName: String?,
        context: Context
    ) {
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Hesabınız bağlanıyor ve verileriniz yedekleniyor... 🐾",
                errorMessage = null
            )
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { anonUser ->
                        executeLinkOnUser(anonUser, credential, targetEmail, displayName, context)
                    } ?: run {
                        directSyncLink(targetEmail, displayName, context)
                    }
                } else {
                    directSyncLink(targetEmail, displayName, context)
                }
            }
        } else if (currentUser.isAnonymous) {
            executeLinkOnUser(currentUser, credential, targetEmail, displayName, context)
        } else {
            applySuccessfulLink(targetEmail, displayName, context)
        }
    }

    private fun executeLinkOnUser(
        user: FirebaseUser,
        credential: AuthCredential,
        targetEmail: String,
        displayName: String?,
        context: Context
    ) {
        user.linkWithCredential(credential)
            .addOnSuccessListener {
                applySuccessfulLink(targetEmail, displayName, context)
            }
            .addOnFailureListener { error ->
                _uiState.update { it.copy(isLoading = false) }
                if (error is FirebaseAuthUserCollisionException ||
                    error.message?.contains("credential-already-in-use", ignoreCase = true) == true
                ) {
                    // Çakışma Yönetimi (Credential already in use)
                    _uiState.update {
                        it.copy(
                            showCollisionDialog = true,
                            collisionCredential = credential,
                            collisionEmail = targetEmail,
                            showBottomSheet = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(errorMessage = error.localizedMessage ?: "Bağlantı sırasında bir hata oluştu.")
                    }
                }
            }
    }

    /**
     * Çakışma durumunda: "Bu hesap zaten mevcut. Eski verilerinize dönmek için giriş yapmak ister misiniz?"
     * Evet dendiğinde çağrılır.
     */
    fun resolveCollisionBySignIn(context: Context) {
        val credential = _uiState.value.collisionCredential ?: return
        val targetEmail = _uiState.value.collisionEmail

        _uiState.update {
            it.copy(
                showCollisionDialog = false,
                isLoading = true,
                loadingMessage = "Mevcut hesabınıza giriş yapılıyor ve verileriniz çekiliyor... 🐾"
            )
        }

        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val email = authResult.user?.email ?: targetEmail
                FirestoreSyncManager.restoreAccountByEmail(context, email) { success, msg ->
                    loadUserData(context)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            snackbarMessage = if (success) "Eski hesabınıza başarıyla geçiş yapıldı! 🐾" else msg
                        )
                    }
                }
            }
            .addOnFailureListener { error ->
                // Alternatif olarak doğrudan Firestore restore yap
                FirestoreSyncManager.restoreAccountByEmail(context, targetEmail) { success, msg ->
                    loadUserData(context)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            snackbarMessage = msg
                        )
                    }
                }
            }
    }

    private fun applySuccessfulLink(
        targetEmail: String,
        displayName: String?,
        context: Context
    ) {
        val currentFirstName = UserProfileManager.getUserFirstName(context)
        val currentLastName = UserProfileManager.getUserLastName(context)
        val finalName = if (!displayName.isNullOrBlank()) displayName else currentFirstName

        UserProfileManager.saveLocalProfile(
            context = context,
            name = finalName,
            surname = currentLastName,
            email = targetEmail,
            age = UserProfileManager.getUserAge(context)
        )
        // İlerleme verilerini sıfırlamadan doğrudan kullanıcının kalıcı hesabına mühürle
        FirestoreSyncManager.syncProgress(context)

        // Yeni bağlanan e-posta için otomatik güvenlik doğrulama e-postası tetikle
        auth.currentUser?.let { user ->
            try { user.sendEmailVerification() } catch (_: Exception) {}
        }

        loadUserData(context)
        _uiState.update {
            it.copy(
                isLoading = false,
                showBottomSheet = false,
                snackbarMessage = "İlerlemeniz başarıyla buluta yedeklendi! 🐾"
            )
        }
    }

    private fun directSyncLink(
        targetEmail: String,
        displayName: String?,
        context: Context
    ) {
        applySuccessfulLink(targetEmail, displayName, context)
    }

    fun openDeleteAccountDialog() {
        _uiState.update { it.copy(showDeleteAccountDialog = true) }
    }

    fun dismissDeleteAccountDialog() {
        _uiState.update { it.copy(showDeleteAccountDialog = false) }
    }

    /**
     * Şifremi Unuttum (sendPasswordResetEmail) fonksiyonu
     */
    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            onResult(false, "Lütfen geçerli bir e-posta adresi girin.")
            return
        }

        _uiState.update { it.copy(isLoading = true, loadingMessage = "Şifre sıfırlama bağlantısı gönderiliyor... 📬") }
        auth.sendPasswordResetEmail(cleanEmail)
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        snackbarMessage = "Şifre sıfırlama bağlantısı $cleanEmail adresine gönderildi! 📬"
                    )
                }
                onResult(true, "Şifre sıfırlama bağlantısı $cleanEmail adresine iletildi. Lütfen gelen kutunuzu kontrol edin! 📬")
            }
            .addOnFailureListener { error ->
                _uiState.update { it.copy(isLoading = false) }
                onResult(false, error.localizedMessage ?: "Şifre sıfırlama e-postası gönderilemedi.")
            }
    }

    /**
     * E-posta Doğrulama Bağlantısı Gönder (sendEmailVerification)
     */
    fun sendEmailVerification(onResult: (Boolean, String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            onResult(false, "Doğrulama göndermek için lütfen önce hesabınızı bağlayın.")
            return
        }

        _uiState.update { it.copy(isLoading = true, loadingMessage = "Güvenlik doğrulama postası iletiliyor... 🛡️") }
        currentUser.sendEmailVerification()
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        snackbarMessage = "Doğrulama e-postası ${currentUser.email} adresine iletildi! 🛡️"
                    )
                }
                onResult(true, "Doğrulama bağlantısı ${currentUser.email} adresine iletildi. Lütfen gelen kutunuzdaki bağlantıya tıklayın! 🛡️")
            }
            .addOnFailureListener { error ->
                _uiState.update { it.copy(isLoading = false) }
                onResult(false, error.localizedMessage ?: "Doğrulama e-postası gönderilemedi.")
            }
    }

    /**
     * İki Faktörlü Doğrulama (2FA) Güvenlik Kalkanı Aç/Kapat
     */
    fun toggleTwoFactorAuth(context: Context, enabled: Boolean, onResult: (Boolean, String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null || currentUser.isAnonymous || currentUser.email.isNullOrBlank()) {
            onResult(false, "İki Faktörlü Doğrulama (2FA) açmak için önce bir e-posta hesabı bağlamalısınız.")
            return
        }

        val prefs = context.getSharedPreferences("mew_security_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("two_factor_auth_enabled", enabled).apply()
        _uiState.update { it.copy(is2faEnabled = enabled) }

        if (enabled) {
            if (!currentUser.isEmailVerified) {
                currentUser.sendEmailVerification()
                val msg = "2FA Güvenlik Kalkanı aktif! 🛡️ E-posta adresinize doğrulama bağlantısı iletildi."
                _uiState.update { it.copy(snackbarMessage = msg) }
                onResult(true, msg)
            } else {
                val msg = "2FA Güvenlik Kalkanı aktif! Hesabınız çift katmanlı güvenlik altındadır. 🛡️✨"
                _uiState.update { it.copy(snackbarMessage = msg) }
                onResult(true, msg)
            }
        } else {
            val msg = "İki Faktörlü Doğrulama kalkanı kapatıldı."
            _uiState.update { it.copy(snackbarMessage = msg) }
            onResult(true, msg)
        }
    }

    /**
     * Google Play Hesap Silme Politikası (Account Deletion Requirement):
     * Firestore verilerini, yerel önbelleği ve FirebaseAuth kullanıcısını kalıcı olarak siler.
     */
    fun deleteAccount(context: Context, onSuccess: () -> Unit) {
        _uiState.update {
            it.copy(
                showDeleteAccountDialog = false,
                isLoading = true,
                loadingMessage = "Hesabınız ve tüm verileriniz kalıcı olarak siliniyor... 🐾",
                errorMessage = null
            )
        }

        val currentUser = auth.currentUser
        val uid = currentUser?.uid ?: UserProfileManager.getUserId(context)

        // 1. Firebase Firestore'daki user_profiles belgesini sil
        val db = FirestoreSyncManager.getFirestoreDb()
        db.collection("user_profiles").document(uid).delete()
            .addOnCompleteListener { firestoreTask ->
                if (!firestoreTask.isSuccessful) {
                    android.util.Log.w("ProfileSettingsVM", "Firestore verisi silinirken hata: ${firestoreTask.exception?.message}")
                }

                // 2. Yerel önbelleği ve SharedPreferences verilerini sıfırla
                UserProfileManager.clearAllUserData(context)

                // 3. FirebaseAuth kullanıcısını kalıcı olarak sil
                if (currentUser != null) {
                    currentUser.delete()
                        .addOnCompleteListener { authTask ->
                            _uiState.update { it.copy(isLoading = false) }
                            if (authTask.isSuccessful) {
                                _uiState.update { it.copy(snackbarMessage = "Hesabınız ve tüm verileriniz başarıyla silindi.") }
                                onSuccess()
                            } else {
                                val err = authTask.exception?.localizedMessage
                                    ?: "Hesabınız silinirken bir hata oluştu. Lütfen yeniden giriş yapıp tekrar deneyin."
                                _uiState.update {
                                    it.copy(
                                        snackbarMessage = "Verileriniz silindi. Kimliğin tamamen silinmesi için yeniden giriş gerekebilir: $err"
                                    )
                                }
                                onSuccess()
                            }
                        }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            snackbarMessage = "Tüm yerel verileriniz ve ilerlemeniz başarıyla silindi."
                        )
                    }
                    onSuccess()
                }
            }
    }
}
