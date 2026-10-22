package com.example.ngilizcequizapp

import android.app.Activity
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeOnboardingScreen(
    onRegistrationSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Sekme durumu: 0 = Yeni Kayıt Ol, 1 = Zaten Hesabım Var (Giriş Yap)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Form alanları
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }

    // Yüklenme & Hata Durumları
    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("Lütfen bekleyin...") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showKvkkDialog by remember { mutableStateOf(false) }
    var showGoogleNoticeDialog by remember { mutableStateOf<String?>(null) }

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
                Color(0xFF6366F1).copy(alpha = 0.6f),
                Color(0xFF06B6D4).copy(alpha = 0.6f),
                Color(0xFFFFD700).copy(alpha = 0.4f)
            )
        )
    }

    val buttonBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF6366F1),
                Color(0xFF4F46E5),
                Color(0xFF06B6D4)
            )
        )
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

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val gEmail = account.email ?: ""
                    val gFirst = account.givenName ?: account.displayName?.split(" ")?.firstOrNull() ?: "Mew"
                    val gLast = account.familyName ?: account.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: ""

                    loadingMessage = "Google hesabınız doğrulanıyor ve verileriniz yükleniyor... 🐾"
                    isLoading = true

                    // Google ile giriş: Doğrudan admin paneli / Firestore'dan verileri çek veya kaydet
                    FirestoreSyncManager.restoreAccountByEmail(
                        context = context,
                        email = gEmail,
                        enteredName = gFirst,
                        enteredSurname = gLast,
                        enteredAge = 18
                    ) { success, msg ->
                        isLoading = false
                        if (success) {
                            Toast.makeText(context, "Hoş geldin, $gFirst! 🐾", Toast.LENGTH_SHORT).show()
                            onRegistrationSuccess()
                        } else {
                            showGoogleNoticeDialog = msg
                        }
                    }
                } else {
                    isLoading = false
                }
            } catch (e: Exception) {
                isLoading = false
                showGoogleNoticeDialog = "Google hesabı seçilemedi: ${e.message}\n\nDilerseniz 'Zaten Hesabım Var' sekmesine e-postanızı yazarak hemen giriş yapabilirsiniz."
            }
        } else {
            isLoading = false
        }
    }

    // 1. Yeni Kayıt Ol İşlemi (Ad, Soyad, E-posta, Yaş İstenir)
    fun handleManualRegister() {
        val trimmedFirst = firstName.trim()
        val trimmedLast = lastName.trim()
        val trimmedEmail = emailText.trim()
        val parsedAge = ageText.trim().toIntOrNull()

        if (trimmedFirst.length < 2) {
            errorMessage = "Lütfen geçerli bir ad girin (en az 2 harf)."
            return
        }
        if (trimmedLast.length < 2) {
            errorMessage = "Lütfen geçerli bir soyad girin (en az 2 harf)."
            return
        }
        if (trimmedEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = "Lütfen geçerli bir e-posta adresi girin (örn: isim@gmail.com)."
            return
        }
        if (parsedAge == null || parsedAge !in 5..120) {
            errorMessage = "Lütfen geçerli bir yaş girin (5 - 120)."
            return
        }

        errorMessage = null
        loadingMessage = "Hesabınız oluşturuluyor ve admin paneline kaydediliyor... 🐾"
        isLoading = true

        UserProfileManager.registerUser(
            context = context,
            name = trimmedFirst,
            surname = trimmedLast,
            email = trimmedEmail,
            age = parsedAge
        ) { success, error ->
            isLoading = false
            if (success) {
                Toast.makeText(context, "Hoş geldin, $trimmedFirst! 🐾", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Kayıt açıldı.", Toast.LENGTH_SHORT).show()
            }
            onRegistrationSuccess()
        }
    }

    // 2. "Zaten Hesabım Var" (SADECE MAİL YAZILIR, İSİM SOYİSİM YAŞ BOŞ BIRAKILIR, SİSTEM AÇILIR)
    fun handleRestoreAccount() {
        val trimmedEmail = emailText.trim()

        // 🚀 DEMO / TEST BYPASS MODU
        if (trimmedEmail.equals("google_test@gmail.com", ignoreCase = true)) {
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
            Toast.makeText(context, "Demo Modu Bypass ile Başarıyla Açıldı! 🚀", Toast.LENGTH_SHORT).show()
            onRegistrationSuccess()
            return
        }

        if (trimmedEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = "Lütfen geçerli bir e-posta adresi girin (örn: isim@gmail.com)."
            return
        }

        errorMessage = null
        loadingMessage = "Admin panelinden verileriniz çekiliyor... 🐾\n(Coin, Elmas, XP ve Seriler yükleniyor)"
        isLoading = true

        FirestoreSyncManager.restoreAccountByEmail(
            context = context,
            email = trimmedEmail,
            enteredName = "",
            enteredSurname = "",
            enteredAge = 0
        ) { success, msg ->
            isLoading = false
            if (success) {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                onRegistrationSuccess()
            } else {
                errorMessage = msg
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(10.dp))

            // Mew Maskot ve Işıma
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(3.dp, cardBorderBrush, CircleShape)
                    .shadow(16.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.akademisyen_mew),
                    contentDescription = "Mew Maskotu",
                    modifier = Modifier.size(76.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(14.dp))

            // Hoş Geldin Başlığı
            Text(
                text = "MewEnglish'e Hoş Geldin!",
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "İngilizce öğrenme yolculuğuna başla veya mevcut hesabını admin panelinden tek tıkla yükle ✨",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(18.dp))

            // 1. GOOGLE İLE TEK TIKLA DEVAM ET BUTONU (HER ZAMAN GÖRÜNÜR)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoading) {
                        loadingMessage = "Google hesabınıza bağlanılıyor... 🐾"
                        isLoading = true
                        errorMessage = null
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleAuthLauncher.launch(googleSignInClient.signInIntent)
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFFFFF),
                shadowElevation = 6.dp,
                border = BorderStroke(1.5.dp, Color(0xFFE2E8F0))
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4285F4)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Google ile Tek Tıkla Devam Et",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // 2. SEKME BUTONLARI: [YENİ KAYIT] vs [ZATEN HESABIM VAR]
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sekme 1: Yeni Kayıt Ol
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedTab == 0) Color(0xFF6366F1) else Color.Transparent
                            )
                            .clickable {
                                selectedTab = 0
                                errorMessage = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = if (selectedTab == 0) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Yeni Kayıt",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Sekme 2: Zaten Hesabım Var
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedTab == 1) Color(0xFF06B6D4) else Color.Transparent
                            )
                            .clickable {
                                selectedTab = 1
                                errorMessage = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = if (selectedTab == 1) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Zaten Hesabım Var",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // 3. FORM KARTI
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorderBrush, RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF131D38).copy(alpha = 0.85f),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    // Sekme Açıklaması
                    Text(
                        text = if (selectedTab == 0) {
                            "✨ Yeni profil oluşturmak için bilgilerinizi girin:"
                        } else {
                            "🐾 Google veya Mew hesabınızın e-posta adresini girmeniz yeterlidir. İsim, yaş ve puanlarınız otomatik çekilecektir:"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF38BDF8),
                        lineHeight = 17.sp
                    )

                    // E-posta Adresi (Her iki modda da gösterilir)
                    OutlinedTextField(
                        value = emailText,
                        onValueChange = {
                            emailText = it
                            errorMessage = null
                        },
                        label = { Text("E-posta Adresiniz", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("Örn: ali@gmail.com", color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF38BDF8))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = if (selectedTab == 0) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            onDone = {
                                focusManager.clearFocus()
                                if (selectedTab == 1) handleRestoreAccount()
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF38BDF8)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // YALNIZCA "YENİ KAYIT" SEKMESİNDE AD, SOYAD VE YAŞ İSTENİR
                    // "ZATEN HESABIM VAR" SEÇİLİRSE BU ALANLAR BOŞ BIRAKILIR VE GİZLENİR!
                    if (selectedTab == 0) {
                        // Ad
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = {
                                firstName = it
                                errorMessage = null
                            },
                            label = { Text("Adınız", color = Color(0xFF94A3B8)) },
                            placeholder = { Text("Örn: Ali", color = Color(0xFF64748B)) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6366F1))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Soyad
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = {
                                lastName = it
                                errorMessage = null
                            },
                            label = { Text("Soyadınız", color = Color(0xFF94A3B8)) },
                            placeholder = { Text("Örn: Yılmaz", color = Color(0xFF64748B)) },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF06B6D4))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF06B6D4),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF06B6D4)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Yaş
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = {
                                if (it.length <= 3 && it.all { ch -> ch.isDigit() }) {
                                    ageText = it
                                    errorMessage = null
                                }
                            },
                            label = { Text("Yaşınız", color = Color(0xFF94A3B8)) },
                            placeholder = { Text("Örn: 20", color = Color(0xFF64748B)) },
                            leadingIcon = {
                                Icon(Icons.Default.Cake, contentDescription = null, tint = Color(0xFFFFD700))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    handleManualRegister()
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFFFFD700)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Hata Mesajı
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        errorMessage?.let { msg ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = msg,
                                        color = Color(0xFFEF4444),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // KVKK Bilgilendirmesi (Yeni Kayıt modunda)
                    if (selectedTab == 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Kayıt olarak ",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "KVKK Aydınlatma Metni'ni",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable { showKvkkDialog = true }
                            )
                            Text(
                                text = " kabul edersiniz.",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Aksiyon Butonu
                    Button(
                        onClick = {
                            if (selectedTab == 0) handleManualRegister() else handleRestoreAccount()
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selectedTab == 0) buttonBrush else Brush.horizontalGradient(
                                    listOf(Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF6366F1))
                                ),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (selectedTab == 0) "Maceraya Başla" else "Giriş Yap ve Başla 🚀",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                if (selectedTab == 0) Icons.Default.RocketLaunch else Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
        }

        // 4. FULL-SCREEN YÜKLENME EKRANI (LOADING OVERLAY)
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Dialog(
                onDismissRequest = { /* İşlem sürerken dışarı tıklanarak kapatılamaz */ },
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
                            .padding(28.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                                .border(2.dp, Color(0xFF38BDF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.akademisyen_mew),
                                contentDescription = null,
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        CircularProgressIndicator(
                            modifier = Modifier.size(42.dp),
                            color = Color(0xFF06B6D4),
                            strokeWidth = 3.5.dp
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = loadingMessage,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Lütfen bekleyin...",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 5. GOOGLE GİRİŞ BİLGİLENDİRME / HATA DİYALOĞU
        showGoogleNoticeDialog?.let { notice ->
            AlertDialog(
                onDismissRequest = { showGoogleNoticeDialog = null },
                containerColor = Color(0xFF131D38),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFFD700))
                        Spacer(Modifier.width(8.dp))
                        Text("Google Giriş Bildirimi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Text(notice, color = Color(0xFFCBD5E1), fontSize = 13.sp, lineHeight = 19.sp)
                },
                confirmButton = {
                    TextButton(onClick = { showGoogleNoticeDialog = null }) {
                        Text("Anladım", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 6. KVKK AYDINLATMA METNİ DİYALOĞU
        if (showKvkkDialog) {
            AlertDialog(
                onDismissRequest = { showKvkkDialog = false },
                containerColor = Color(0xFF0F172A),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF6366F1))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "KVKK Aydınlatma Metni",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = """
                            MewEnglish olarak kişisel verilerinizin 6698 sayılı Kişisel Verilerin Korunması Kanunu'na (KVKK) uygun şekilde korunmasına büyük önem veriyoruz.
                            
                            1. İşlenen Veriler:
                            Ad, Soyad, E-posta adresi, Yaş, dil öğrenim ilerleme verileri (XP, Coin, Elmas, Seri, Lig, çözülen quizler).
                            
                            2. İşlenme Amacı:
                            Kullanıcı profilinizin oluşturulması, dil öğreniminizin takibi, cihaz değiştiğinde veya yeni girişte puan ve satın alımlarınızın admin paneli üzerinden güvenle geri yüklenmesi ve hesap güvenliğinin sağlanması.
                            
                            3. Aktarım ve Güvenlik:
                            Verileriniz şifreli Google Cloud / Firebase altyapısında saklanmakta olup hiçbir koşulda üçüncü taraflarla paylaşılmaz veya satılmaz.
                            
                            4. Haklarınız:
                            KVKK Madde 11 uyarınca dilediğiniz zaman verilerinizin düzeltilmesini veya silinmesini talep edebilirsiniz.
                            """.trimIndent(),
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showKvkkDialog = false }) {
                        Text("Anladım", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
