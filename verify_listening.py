import json
import re

with open('all_quizzes_dump.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

# Let's inspect 20 listening questions
count = 0
for q in data:
    for qu in q['questions']:
        if qu['type'] == 'LISTENING':
            count += 1
            if count <= 15:
                print(f"[{q['quizNumber']}] qTr: {qu['questionTurkish']} | ans: {qu['correctAnswer']} | opts: {qu['options']}")
