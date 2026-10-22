package com.example.ngilizcequizapp

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ngilizcequizapp.R
import com.example.ngilizcequizapp.UserProfileManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountLinkingDialog(
    title: String = "İlerlemeni Kaybetmemek İçin Mailini Bağla! 🐾",
    description: String = "Harika gidiyorsun! Kazandığın XP, coin ve başarılarının kaybolmaması için hesabını güvene al.",
    onDismiss: () -> Unit,
    onLinkedSuccess: () -> Unit
) {
    val context = LocalContext.current
    val autoEmail = remember { UserProfileManager.getAutoDetectedEmail(context) }
    var email by remember { mutableStateOf(autoEmail) }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showEmailForm by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    // Google Sign-In setup
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val gEmail = account.email ?: ""
                    val gName = account.givenName ?: account.displayName ?: "Mew"
                    val idToken = account.idToken

                    isLoading = true
                    val credential = if (!idToken.isNullOrBlank()) {
                        GoogleAuthProvider.getCredential(idToken, null)
                    } else null

                    val currentUser = auth.currentUser
                    if (currentUser != null && credential != null) {
                        currentUser.linkWithCredential(credential)
                            .addOnSuccessListener {
                                UserProfileManager.saveLocalProfile(
                                    context = context,
                                    name = gName,
                                    surname = "",
                                    email = gEmail,
                                    age = 18
                                )
                                isLoading = false
                                Toast.makeText(context, "Hesabınız başarıyla bağlandı! 🎉", Toast.LENGTH_SHORT).show()
                                onLinkedSuccess()
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                // Even if link fails due to existing user, sync profile
                                UserProfileManager.saveLocalProfile(
                                    context = context,
                                    name = gName,
                                    surname = "",
                                    email = gEmail,
                                    age = 18
                                )
                                Toast.makeText(context, "Hesabınız bağlandı! 🐾", Toast.LENGTH_SHORT).show()
                                onLinkedSuccess()
                            }
                    } else {
                        UserProfileManager.saveLocalProfile(
                            context = context,
                            name = gName,
                            surname = "",
                            email = gEmail,
                            age = 18
                        )
                        isLoading = false
                        Toast.makeText(context, "Hesap bağlandı! 🐾", Toast.LENGTH_SHORT).show()
                        onLinkedSuccess()
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Google girişi tamamlanamadı: ${e.message}"
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF06B6D4), Color(0xFFFFD700)))),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Kapat Butonu (Sağ Üst)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                // Maskot Görseli
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(2.dp, Color(0xFF6366F1), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.akademisyen_mew),
                        contentDescription = null,
                        modifier = Modifier.size(66.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Başlık
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Açıklama
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF6366F1),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    // 1. Google İle Bağla Butonu
                    Button(
                        onClick = {
                            errorMessage = null
                            googleSignInClient.signOut().addOnCompleteListener {
                                googleLauncher.launch(googleSignInClient.signInIntent)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "🌐 Google ile Hızlı Bağla",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. E-posta ile Bağlama Aç/Kapa
                    OutlinedButton(
                        onClick = { showEmailForm = !showEmailForm },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = if (showEmailForm) "E-posta Formunu Gizle" else "✉️ E-posta & Şifre ile Bağla",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    AnimatedVisibility(visible = showEmailForm) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (autoEmail.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { email = autoEmail }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("⚡ Algılanan Google Hesabı: ", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        Text(autoEmail, fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("E-posta Adresi", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF6366F1),
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Şifre (En az 6 karakter)", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF6366F1),
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
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
                                            val cleanEmail = email.trim()
                                            if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                                                errorMessage = "Lütfen önce şifresini sıfırlamak istediğiniz e-posta adresinizi girin."
                                            } else {
                                                isLoading = true
                                                errorMessage = null
                                                auth.sendPasswordResetEmail(cleanEmail)
                                                    .addOnSuccessListener {
                                                        isLoading = false
                                                        Toast.makeText(context, "Şifre sıfırlama bağlantısı $cleanEmail adresine gönderildi! 📬", Toast.LENGTH_LONG).show()
                                                    }
                                                    .addOnFailureListener { err ->
                                                        isLoading = false
                                                        errorMessage = err.localizedMessage ?: "Şifre sıfırlama e-postası gönderilemedi."
                                                    }
                                            }
                                        }
                                        .padding(vertical = 2.dp, horizontal = 2.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    val cleanEmail = email.trim()
                                    // Demo bypass check
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
                                        Toast.makeText(context, "Demo Mod Açıldı! 🚀", Toast.LENGTH_SHORT).show()
                                        onLinkedSuccess()
                                        return@Button
                                    }

                                    if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                                        errorMessage = "Lütfen geçerli bir e-posta adresi girin."
                                        return@Button
                                    }
                                    if (password.length < 6) {
                                        errorMessage = "Şifreniz en az 6 karakter olmalıdır."
                                        return@Button
                                    }

                                    isLoading = true
                                    errorMessage = null
                                    val credential = EmailAuthProvider.getCredential(cleanEmail, password)
                                    val currentUser = auth.currentUser
                                    if (currentUser != null && currentUser.isAnonymous) {
                                        currentUser.linkWithCredential(credential)
                                            .addOnSuccessListener {
                                                try { currentUser.sendEmailVerification() } catch (_: Exception) {}
                                                UserProfileManager.saveLocalProfile(
                                                    context = context,
                                                    name = cleanEmail.substringBefore("@"),
                                                    surname = "",
                                                    email = cleanEmail,
                                                    age = 18
                                                )
                                                isLoading = false
                                                Toast.makeText(context, "Hesabınız başarıyla bağlandı! Doğrulama postası iletildi 🐾", Toast.LENGTH_SHORT).show()
                                                onLinkedSuccess()
                                            }
                                            .addOnFailureListener { err ->
                                                isLoading = false
                                                errorMessage = err.localizedMessage ?: "Bağlama başarısız."
                                            }
                                    } else {
                                        UserProfileManager.saveLocalProfile(
                                            context = context,
                                            name = cleanEmail.substringBefore("@"),
                                            surname = "",
                                            email = cleanEmail,
                                            age = 18
                                        )
                                        isLoading = false
                                        onLinkedSuccess()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                            ) {
                                Text("Bağlantıyı Tamamla", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Hayır / Kapat Butonu
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Hayır / Daha Sonra Hatırlat",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
