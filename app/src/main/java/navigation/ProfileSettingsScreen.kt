package com.example.ngilizcequizapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.filled.DeleteForever
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ngilizcequizapp.ProfileSettingsViewModel
import com.example.ngilizcequizapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileSettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val appColors = LocalAppThemeColors.current

    val uiState by viewModel.uiState.collectAsState()

    // Ekrana girildiğinde verileri yükle
    LaunchedEffect(Unit) {
        viewModel.loadUserData(context)
    }

    // Bildirim mesajları için Toast dinleyicisi
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearSnackbarMessage()
        }
    }

    // Google Sign-In Yapılandırması
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val gEmail = account.email ?: ""
                    val gName = account.givenName ?: account.displayName?.split(" ")?.firstOrNull() ?: "Mew"
                    val idToken = account.idToken
                    viewModel.linkWithGoogle(idToken, gEmail, gName, context)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Google hesabı seçilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // E-posta ile bağlama alanları state'i (Google e-postası otomatik algılanır)
    val autoDetectedEmail = remember { UserProfileManager.getAutoDetectedEmail(context) }
    var emailInput by remember { mutableStateOf(autoDetectedEmail) }
    var passwordInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.showBottomSheet) {
        if (uiState.showBottomSheet && emailInput.isBlank()) {
            val detected = UserProfileManager.getAutoDetectedEmail(context)
            if (detected.isNotBlank()) emailInput = detected
        }
    }

    val bgGradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0F24),
                Color(0xFF0F1738),
                Color(0xFF060B1C)
            )
        )
    }

    val cardBorderBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF6366F1).copy(alpha = 0.5f),
                Color(0xFF06B6D4).copy(alpha = 0.5f),
                Color(0xFFFFD700).copy(alpha = 0.3f)
            )
        )
    }

    val primaryButtonBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF6366F1),
                Color(0xFF4F46E5),
                Color(0xFF06B6D4)
            )
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil & Ayarlar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // =========================================================================
            // 1. ÜST / ORTA ALAN: PROFİL RESMİ, RANK ÇERÇEVESİ & MAİL BAĞLA BUTONU
            // =========================================================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorderBrush, RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                color = Color(0xFF131D38).copy(alpha = 0.90f),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tam ortada: Kullanıcının Avatarı & Symmetrical Rank Kanadı / Çerçevesi
                    // Ve hemen köşesinde / yanında dikkat çekici "Mail Bağla" (veya minik onay rozeti)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 8.dp)
                    ) {
                        ProfileAvatarWithWings(
                            avatarRes = uiState.avatarRes,
                            wingDrawableRes = uiState.wingDrawableRes,
                            wingHeight = 96.dp
                        )

                        // Rank Çerçevesinin hemen köşesinde/yanında konumlanan buton / rozet
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 6.dp, top = 2.dp)
                        ) {
                            if (!uiState.isLinked) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { viewModel.openBottomSheet() },
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.Transparent,
                                    shadowElevation = 8.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF6366F1), Color(0xFF06B6D4))
                                                ),
                                                RoundedCornerShape(20.dp)
                                            )
                                            .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                                            .padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Mail Bağla",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF064E3B).copy(alpha = 0.9f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Yedeklendi",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Kullanıcı Adı
                    Text(
                        text = uiState.userFullName,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Mevcut Lig Derecesi Rozeti
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = uiState.rank.color.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, uiState.rank.color.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = uiState.rank.color,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${uiState.rank.nameStr.uppercase()} LİGİ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = uiState.rank.color
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // =====================================================================
                    // MAIL BAĞLA / YEDEKLE BUTONU (VEYA BAĞLI HESAP BİLGİSİ)
                    // =====================================================================
                    if (!uiState.isLinked) {
                        // KULLANICI HESABINI BAĞLAMAMIŞSA: DİKKAT ÇEKİCİ "MAIL BAĞLA" BUTONU
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .clickable { viewModel.openBottomSheet() },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            shadowElevation = 6.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(primaryButtonBrush, RoundedCornerShape(16.dp))
                                    .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = "Mail Bağla (Hesabı Yedekle)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "İlerlemeni buluta mühürle ve kaybetme ✨",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // KULLANICI ZATEN BAĞLAMIŞSA: MİNİK ONAY ROZETİ VE BAĞLI E-POSTA
                        Surface(
                            modifier = Modifier.fillMaxWidth(0.92f),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0F2B1F).copy(alpha = 0.85f),
                            border = BorderStroke(1.2.dp, Color(0xFF10B981).copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Bağlı Hesap",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = uiState.userEmail,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "✓ İlerlemeniz Buluta Yedeklendi",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF34D399)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // =========================================================================
            // 2. ALT ALAN: İSTATİSTİK VE İLERLEME KARTLARI (COIN, ELMAS, XP, RANK, KELİME)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "İstatistik ve İlerleme Kartları",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 1. Satır: Coin & Elmas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatCard(
                    title = "Altın (Coin)",
                    value = "${uiState.coins}",
                    icon = Icons.Default.MonetizationOn,
                    iconTint = Color(0xFFFFD700),
                    cardBg = Color(0xFF1E1E38),
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    title = "Elmas (Gems)",
                    value = "${uiState.gems}",
                    icon = Icons.Default.Bolt,
                    iconTint = Color(0xFF06B6D4),
                    cardBg = Color(0xFF132238),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Satır: Toplam Skor (XP) & Ezberlenen Kelime Sayısı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatCard(
                    title = "Toplam Skor (XP)",
                    value = "${uiState.xp} XP",
                    icon = Icons.Default.Star,
                    iconTint = Color(0xFFA855F7),
                    cardBg = Color(0xFF221538),
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    title = "Ezberlenen Kelime",
                    value = "${uiState.savedWordsCount} Kelime",
                    icon = Icons.Default.MenuBook,
                    iconTint = Color(0xFF10B981),
                    cardBg = Color(0xFF122828),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Satır: Mevcut Lig Derecesi (Rank Kartı - Tam Genişlik)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF161E3D).copy(alpha = 0.85f),
                border = BorderStroke(1.dp, uiState.rank.color.copy(alpha = 0.4f)),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(uiState.rank.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = uiState.rank.color,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Mevcut Lig Derecesi",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = uiState.rank.nameStr,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = uiState.rank.color
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Text(
                            text = "Aktif",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =============================================================================
            // GOOGLE PLAY ABONELİK YÖNETİMİ
            // =============================================================================
            if (AppConfig.IS_MARKET_ENABLED || UserProfileManager.isPremium(context)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF131D38).copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Abonelik Yönetimi",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Google Play üzerinden aktif aboneliklerinizi yönetebilir veya iptal edebilirsiniz.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { openSubscriptionManagement(context) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Google Play Aboneliklerini Aç", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // =============================================================================
            // HESAP GÜVENLİĞİ VE İKİ FAKTÖRLÜ DOĞRULAMA (2FA) KALKANI
            // =============================================================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF131F37).copy(alpha = 0.9f),
                border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFF06B6D4), Color(0xFF6366F1)))),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0EA5E9).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hesap Güvenliği & 2FA Kalkanı",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "İki Faktörlü Kimlik Doğrulama & E-posta Koruması",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // E-posta Doğrulama Durumu
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isEmailVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (uiState.isEmailVerified) Color(0xFF10B981) else Color(0xFFFFB703),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.isEmailVerified) "E-posta Doğrulandı ✓" else "E-posta Doğrulaması Bekliyor",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.isEmailVerified) Color(0xFF34D399) else Color(0xFFFFD166)
                            )
                            Text(
                                text = if (uiState.isEmailVerified) "Hesabınız doğrulanmış güvenli hesaptır." else "Güvenlik onay bağlantısı göndererek e-postanızı doğrulayın.",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        if (!uiState.isEmailVerified && uiState.isLinked) {
                            TextButton(
                                onClick = {
                                    viewModel.sendEmailVerification { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Doğrula", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2FA Kalkanı Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "İki Faktörlü Doğrulama (2FA)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Cihaz veya şifre değişikliklerinde e-posta onayı ister.",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Switch(
                            checked = uiState.is2faEnabled,
                            onCheckedChange = { checked ->
                                viewModel.toggleTwoFactorAuth(context, checked) { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF06B6D4),
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = Color(0xFF334155)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =============================================================================
            // GOOGLE PLAY HESAP VE VERİ SİLME ALANI (ACCOUNT DELETION)
            // =============================================================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF22111E).copy(alpha = 0.9f),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hesap ve Gizlilik Ayarları",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hesabınızı ve sunucularımızda saklanan tüm ilerlemenizi kalıcı olarak silebilirsiniz. Bu işlem geri alınamaz.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.openDeleteAccountDialog() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hesabımı ve Verilerimi Sil", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // =============================================================================
        // 4. "MAIL BAĞLA" BOTTOMSHEET (ACCOUNT LINKING)
        // =============================================================================
        if (uiState.showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeBottomSheet() },
                containerColor = Color(0xFF101833),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(42.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF334155))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mew Maskot
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(2.dp, Color(0xFF38BDF8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.akademisyen_mew),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Hesabını Bağla ve Yedekle 🐾",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Mevcut XP, coin, elmas ve kelimelerin sıfırlanmadan doğrudan kalıcı hesabına mühürlenir. Cihaz değiştirsen bile kaldığın yerden devam edersin ✨",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. ÖNCELİKLİ SEÇENEK: GOOGLE İLE DEVAM ET
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                googleSignInClient.signOut().addOnCompleteListener {
                                    googleLauncher.launch(googleSignInClient.signInIntent)
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 13.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "G",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4285F4)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Google ile Devam Et",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // VEYA ÇİZGİSİ
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF334155))
                        Text(
                            text = "veya e-posta ile bağla",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF334155))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. SEÇENEK: E-POSTA VE ŞİFRE İLE BAĞLAMA FORMU
                    if (autoDetectedEmail.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { emailInput = autoDetectedEmail }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚡ Algılanan Google Hesabı: ", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(autoDetectedEmail, fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("E-posta Adresiniz", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("Örn: isim@gmail.com", color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF38BDF8))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Şifreniz (En az 6 karakter)", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6366F1))
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.linkWithEmail(emailInput, passwordInput, context)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Şifremi Unuttum Bağlantısı
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Şifremi Unuttum?",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable {
                                    focusManager.clearFocus()
                                    viewModel.sendPasswordResetEmail(emailInput) { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        )
                    }

                    // Hata Mesajı
                    uiState.errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = err,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bağla Butonu
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.linkWithEmail(emailInput, passwordInput, context)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(vertical = 13.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(primaryButtonBrush, RoundedCornerShape(14.dp))
                    ) {
                        Text(
                            text = "E-postayı Bağla ve Mühürle 🔒",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // =============================================================================
        // 5. ÇAKIŞMA YÖNETİMİ DİYALOĞU (CREDENTIAL ALREADY IN USE)
        // =============================================================================
        if (uiState.showCollisionDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissCollisionDialog() },
                containerColor = Color(0xFF131D38),
                shape = RoundedCornerShape(22.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Hesap Zaten Mevcut",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "Bu e-posta adresi (${uiState.collisionEmail}) daha önce başka bir hesapta kullanılmış.\n\nEski verilerinize dönmek ve kaldığınız yerden devam etmek için giriş yapmak ister misiniz?",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resolveCollisionBySignIn(context) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                    ) {
                        Text(
                            text = "Evet, Eski Verilerime Dön",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissCollisionDialog() }) {
                        Text("Vazgeç", color = Color(0xFF94A3B8))
                    }
                }
            )
        }

        // =============================================================================
        // HESAP SİLME ONAY DİYALOĞU (ACCOUNT DELETION CONFIRMATION)
        // =============================================================================
        if (uiState.showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDeleteAccountDialog() },
                containerColor = Color(0xFF1E1325),
                shape = RoundedCornerShape(22.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(38.dp)
                    )
                },
                title = {
                    Text(
                        text = "Hesabınızı Silmek İstiyor musunuz?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "Bu işlem GERİ ALINAMAZ!\n\n• Tüm kelime ezberleriniz ve istatistikleriniz\n• Lig dereceniz ve puanlarınız (XP)\n• Altın ve elmas bakiyeniz\n• Bulut sunucularındaki profiliniz\n\ntamamen ve kalıcı olarak silinecektir.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Start
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteAccount(context) {
                                onBack()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text(
                            text = "Evet, Kalıcı Olarak Sil",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDeleteAccountDialog() }) {
                        Text("Vazgeç", color = Color(0xFF94A3B8))
                    }
                }
            )
        }

        // =============================================================================
        // 6. YÜKLENME EKRANI (LOADING OVERLAY)
        // =============================================================================
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Dialog(
                onDismissRequest = { },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0D142E),
                    border = BorderStroke(2.dp, cardBorderBrush),
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(26.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                                .border(2.dp, Color(0xFF38BDF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.akademisyen_mew),
                                contentDescription = null,
                                modifier = Modifier.size(50.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        CircularProgressIndicator(
                            modifier = Modifier.size(38.dp),
                            color = Color(0xFF06B6D4),
                            strokeWidth = 3.dp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = uiState.loadingMessage,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * İstatistik Kartı Bileşeni
 */
@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    cardBg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = cardBg.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, iconTint.copy(alpha = 0.35f)),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}
