# MewEnglish Geliştirme ve Quiz Hazırlama Kuralları

## 🧠 Quiz Hazırlama & Pedagojik Akış Kuralları

### 1. Yeni Konuya Geçiş ve Sıralı Pekiştirme Kuralı (Visual -> Listening Eşleşmesi)
- **Yeni Konuya Başlarken**: Yeni bir konuya geçildiğinde öğretilecek hedef kelimeler (örn: $A, B, C, D$) önce **VISUAL (Görsel Soru)** olarak sunulur.
- **Sıralı Listening Soru Mantığı**:
  - Eğer visual cevabı "$A$" olan bir sorunun ardından bir **LISTENING** sorusu gelecekse, bu listening sorusu mutlaka az önce görseli öğretilen **"$A$" kelimesi/kavramı ile doğrudan ilişkili** olmalıdır.
  - Cümleler öğrenciyi zorlamayacak, yine tanıtılan "$A$" kelimesi etrafında kurgulanmış net ve anlaşılır yapıda olmalıdır.
  - Ardından sıradaki hedef kelime "$B$" için **VISUAL** soru sunulur, akabindeki listening sorusu yine "$B$" kelimesine odaklanır ($A$ visual $\rightarrow$ $A$ listening $\rightarrow$ $B$ visual $\rightarrow$ $B$ listening $\rightarrow \dots$).
- **Amaç**: Öğrencinin daha önce görmediği bir kelimeyi doğrudan dinlemede tek başına görüp zorlanmasını engellemek; önce görselle kelimeyi zihninde canlandırmasını, hemen ardından dinlemeyle pekiştirmesini sağlamaktır.

---

### 2. UI & Ekran Tasarım Kuralları
- **Quiz Boyut Dengesi**:
  - Quiz ekranlarında (Visual, Listening, Match, Fill-in-the-blank) soru resimleri, metinleri ve kontrol tuşları ekrana ne çok büyük (taşacak şekilde) ne de aşırı küçük/sönük kalacak şekilde dengeli olmalıdır.
  - Görsel soru şıkları (`LazyVerticalGrid`): Resimler maksimum `92.dp`, `aspectRatio(1.10f)` ve metinler `18sp` bold olmalıdır.
  - Dinleme sorusu: Karakter resmi en az `100-105.dp`, konuşma balonu metni `20sp` bold, şık butonları en az `60.dp` yükseklikte ve `18sp` bold olmalıdır.
  - Hata Torbası (`MistakePracticeScreen` ve `MistakeReviewScreen`): Ana quiz tasarımındaki görsel ve metin boyutlarıyla birebir aynı kalitede olmalıdır.
