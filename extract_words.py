import json
import re
from collections import OrderedDict

# Load dumped quizzes
with open('all_quizzes_dump.json', 'r', encoding='utf-8') as f:
    quizzes = json.load(f)

# Turkish character regex
tr_chars = re.compile(r'[çğıöşüÇĞİÖŞÜ]')

# Common Turkish words that don't have Turkish-specific characters
tr_word_list = {
    'ben', 'sen', 'o', 'biz', 'siz', 'onlar', 'bir', 've', 'ile', 'bu', 'su', 'icin',
    'gibi', 'kadar', 'cok', 'daha', 'en', 'var', 'yok', 'evet', 'hayir', 'merhaba',
    'gunaydin', 'iyi', 'kotu', 'elma', 'kedi', 'kopek', 'hangisi', 'hangisidir',
    'dogru', 'yanlis', 'eslestir', 'sec', 'cevir', 'nedir', 'nasil', 'nerede',
    'kim', 'ne', 'zaman', 'tamamla', 'kelime', 'anlami', 'cumle', 'asagidaki',
    'cumleyi', 'uygun', 'doldur', 'boslugu', 'isaretle'
}

def is_turkish_text(text):
    if not text:
        return False
    if tr_chars.search(text):
        return True
    words = [w.lower() for w in re.findall(r'[a-zA-Z]+', text)]
    if not words:
        return False
    tr_matches = sum(1 for w in words if w in tr_word_list)
    return tr_matches > 0 and (tr_matches >= len(words) / 2 or len(words) == 1 and words[0] in tr_word_list)

def clean_and_tokenize(text):
    if not text or is_turkish_text(text):
        return []
    # Replace contractions: don't -> do not, doesn't -> does not, i'm -> i am, etc. or just split words
    # Let's clean punctuation
    text = text.replace("'", " ").replace("’", " ").replace("-", " ")
    tokens = re.findall(r'[a-zA-Z]+', text)
    cleaned = []
    for t in tokens:
        w = t.lower().strip()
        # filter single letters except 'a' and 'i'
        if len(w) == 1 and w not in ('a', 'i'):
            continue
        if len(w) >= 1:
            cleaned.append(w)
    return cleaned

quiz_words_map = OrderedDict()
seen_global = set()

for q in quizzes:
    q_num = q['quizNumber']
    current_quiz_tokens = set()
    
    for qu in q['questions']:
        t = qu['type']
        
        # 1. VISUAL
        if t == 'VISUAL':
            # correctAnswer is English
            if not is_turkish_text(qu['correctAnswer']):
                for w in clean_and_tokenize(qu['correctAnswer']):
                    current_quiz_tokens.add(w)
            # options are English
            for opt in qu['options']:
                if not is_turkish_text(opt):
                    for w in clean_and_tokenize(opt):
                        current_quiz_tokens.add(w)
                        
        # 2. MATCHING
        elif t == 'MATCHING':
            # correctAnswer is comma-separated English tokens
            for part in qu['correctAnswer'].split(','):
                if not is_turkish_text(part):
                    for w in clean_and_tokenize(part):
                        current_quiz_tokens.add(w)
                        
        # 3. TRANSLATION_BUILDER / FILL_IN_BLANK
        elif t in ('TRANSLATION_BUILDER', 'FILL_IN_BLANK'):
            # correctAnswer is English sentence
            if not is_turkish_text(qu['correctAnswer']):
                for w in clean_and_tokenize(qu['correctAnswer']):
                    current_quiz_tokens.add(w)
            for w in qu['correctWords']:
                if not is_turkish_text(w):
                    for tok in clean_and_tokenize(w):
                        current_quiz_tokens.add(tok)
            for w in qu['extraWords']:
                if not is_turkish_text(w):
                    for tok in clean_and_tokenize(w):
                        current_quiz_tokens.add(tok)
            for opt in qu['options']:
                if not is_turkish_text(opt):
                    for tok in clean_and_tokenize(opt):
                        current_quiz_tokens.add(tok)
                        
        # 4. SPEAKING
        elif t == 'SPEAKING':
            for sent in [qu['speakingSentence'], qu['correctAnswer'], qu['questionTurkish']]:
                if not is_turkish_text(sent):
                    for w in clean_and_tokenize(sent):
                        current_quiz_tokens.add(w)
                        
        # 5. PATTERN
        elif t == 'PATTERN':
            for s in [qu['correctAnswer'], qu['patternExampleOne'], qu['patternExampleTwo']]:
                if not is_turkish_text(s):
                    for w in clean_and_tokenize(s):
                        current_quiz_tokens.add(w)
            for opt in qu['options']:
                if not is_turkish_text(opt):
                    for w in clean_and_tokenize(opt):
                        current_quiz_tokens.add(w)
                        
        # 6. LISTENING
        elif t == 'LISTENING':
            # Check questionTurkish
            if not is_turkish_text(qu['questionTurkish']):
                for w in clean_and_tokenize(qu['questionTurkish']):
                    current_quiz_tokens.add(w)
            # Check correctAnswer if English
            if not is_turkish_text(qu['correctAnswer']):
                for w in clean_and_tokenize(qu['correctAnswer']):
                    current_quiz_tokens.add(w)
            # Check options if English
            for opt in qu['options']:
                if not is_turkish_text(opt):
                    for w in clean_and_tokenize(opt):
                        current_quiz_tokens.add(w)

    # Determine brand new words for this quiz
    new_words = sorted(list(current_quiz_tokens - seen_global))
    seen_global.update(new_words)
    
    quiz_words_map[q_num] = {
        'all_words': sorted(list(current_quiz_tokens)),
        'new_words': new_words,
        'new_count': len(new_words)
    }

print(f"Total Quizzes Processed: {len(quiz_words_map)}")
print(f"Total Unique Vocabulary Learned: {len(seen_global)}")

# Print sample quizzes
for q_num in [1, 2, 3, 4, 5, 10, 20, 50, 100, 150, 200, 289, 290, 292, 295, 298, 301]:
    info = quiz_words_map[q_num]
    print(f"Quiz {q_num}: new={info['new_count']} ({', '.join(info['new_words'][:8])}{'...' if len(info['new_words']) > 8 else ''})")
