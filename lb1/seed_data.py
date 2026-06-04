"""
Script to create demo data for the blog project.
Run: python seed_data.py
"""
import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'blog_project.settings')
django.setup()

from django.contrib.auth.models import User
from blog.models import Category, Article, Comment

print("🌱 Створення демо-даних...")

# ── Superuser ─────────────────────────────
if not User.objects.filter(username='admin').exists():
    admin = User.objects.create_superuser('admin', 'admin@context.com', 'admin123')
    print("✓ Superuser: admin / admin123")
else:
    admin = User.objects.get(username='admin')
    print("  Superuser already exists")

# ── Regular users ─────────────────────────
users_data = [
    ('nazar', 'nazar@context.com', 'pass1234'),
    ('olena', 'olena@context.com', 'pass1234'),
]
users = {}
for uname, email, pwd in users_data:
    if not User.objects.filter(username=uname).exists():
        u = User.objects.create_user(uname, email, pwd)
        print(f"✓ User: {uname} / {pwd}")
    else:
        u = User.objects.get(username=uname)
    users[uname] = u

# ── Categories ────────────────────────────
categories_data = [
    ('Програмування', 'Статті про розробку програмного забезпечення, мови програмування та інструменти розробника.'),
    ('Наука і технології', 'Останні відкриття у науці та огляди нових технологій.'),
    ('Дизайн', 'UX/UI дизайн, графіка, типографіка та веб-дизайн.'),
    ('Бізнес', 'Підприємництво, стартапи, менеджмент та фінанси.'),
    ('Освіта', 'Навчання, університет, саморозвиток та онлайн-курси.'),
]
categories = {}
for name, desc in categories_data:
    cat, created = Category.objects.get_or_create(name=name, defaults={'description': desc})
    categories[name] = cat
    if created:
        print(f"✓ Category: {name}")

# ── Articles ──────────────────────────────
articles_data = [
    {
        'title': 'Python у 2025: що нового у мові?',
        'content': '''Python продовжує розвиватися швидкими темпами. У 2025 році вийшла версія Python 3.14, яка принесла безліч покращень.

Серед головних нововведень:
- Покращена продуктивність завдяки новому JIT-компілятору
- Більш читабельні повідомлення про помилки
- Нові типи даних у стандартній бібліотеці
- Покращена підтримка асинхронного програмування

Python залишається однією з найпопулярніших мов програмування у світі, особливо у сфері штучного інтелекту та аналізу даних.

Для вивчення Python рекомендую розпочати з офіційної документації та практичних проєктів. Найкращий спосіб навчитись — це будувати реальні застосунки.''',
        'category': 'Програмування',
        'author': 'nazar',
    },
    {
        'title': 'Django vs FastAPI: що обрати для бекенду?',
        'content': '''Вибір фреймворку для бекенду — одне з ключових рішень при старті нового проєкту. Розберемо переваги Django та FastAPI.

**Django:**
- Повноцінний фреймворк "batteries included"
- Потужна ORM та адмін-панель
- Велика спільнота та безліч бібліотек
- Ідеальний для MVC/MVT застосунків

**FastAPI:**
- Надзвичайно висока продуктивність
- Автоматична документація OpenAPI
- Нативна підтримка async/await
- Чудово підходить для мікросервісів та API

Висновок: якщо будуєте повноцінний веб-застосунок — обирайте Django. Якщо потрібне швидке REST API — FastAPI.''',
        'category': 'Програмування',
        'author': 'nazar',
    },
    {
        'title': 'Штучний інтелект у 2025 році',
        'content': '''Штучний інтелект трансформує кожну галузь. Від медицини до розваг — AI стає невід'ємною частиною нашого життя.

Ключові тренди:
1. Мультимодальні моделі — AI розуміє текст, зображення, звук та відео одночасно
2. Локальні LLM — запуск великих мовних моделей прямо на пристрої користувача
3. AI-агенти — автономні системи, що виконують складні завдання
4. Generative AI у бізнесі — автоматизація робочих процесів

Найближчі роки визначать, як людство буде взаємодіяти з розумними машинами. Важливо розвивати критичне мислення та AI-грамотність.''',
        'category': 'Наука і технології',
        'author': 'olena',
    },
    {
        'title': 'Принципи гарного UI дизайну',
        'content': '''Гарний інтерфейс — це не просто красиві кольори. Це про зручність, логіку та відчуття.

10 принципів хорошого UI:
1. Простота — прибирай зайве
2. Консистентність — однакові елементи мають виглядати однаково
3. Ієрархія — важливе повинно виділятися
4. Зворотній зв'язок — завжди показуй стан системи
5. Доступність — дизайн для всіх
6. Пробіл — "повітря" між елементами покращує читабельність
7. Кольорова гармонія — не більше 3 основних кольорів
8. Типографіка — правильний шрифт змінює все
9. Мобільний спочатку — думай про телефон першим
10. Тестування — перевіряй на реальних користувачах

Практикуйтесь, аналізуйте існуючі продукти та ніколи не зупиняйтесь на вивченні нового.''',
        'category': 'Дизайн',
        'author': 'olena',
    },
    {
        'title': 'Як побудувати успішний стартап',
        'content': '''Стартап — це не просто ідея, це систематична робота над вирішенням реальної проблеми.

Ключові кроки:
1. Знайди реальну проблему — не вигадуй проблему під своє рішення
2. Валідуй ідею — поговори з потенційними клієнтами до написання коду
3. Побудуй MVP — мінімально можливий продукт для тестування гіпотез
4. Ітеруй швидко — отримуй зворотній зв'язок та вдосконалюй
5. Масштабуй — тільки після досягнення product-market fit

Найбільша помилка засновників — закоханість у власну ідею замість закоханості у проблему клієнта.''',
        'category': 'Бізнес',
        'author': 'admin',
    },
]

for data in articles_data:
    if not Article.objects.filter(title=data['title']).exists():
        author = admin if data['author'] == 'admin' else users[data['author']]
        article = Article.objects.create(
            title=data['title'],
            content=data['content'],
            category=categories[data['category']],
            author=author,
        )
        print(f"✓ Article: {data['title'][:50]}")

# ── Comments ──────────────────────────────
comments_data = [
    ('Python у 2025: що нового у мові?', 'olena', 'Чудова стаття! Дуже корисно знати про нові можливості Python.'),
    ('Python у 2025: що нового у мові?', 'admin', 'JIT-компілятор справді дав помітне прискорення у наших проєктах.'),
    ('Django vs FastAPI: що обрати для бекенду?', 'olena', 'Використовую Django вже 3 роки і не уявляю роботу без нього!'),
    ('Штучний інтелект у 2025 році', 'nazar', 'Дуже цікаво! Особливо тема локальних LLM — це майбутнє приватності.'),
    ('Принципи гарного UI дизайну', 'nazar', 'Принцип "мобільний спочатку" змінив мій підхід до роботи. Дякую!'),
    ('Принципи гарного UI дизайну', 'admin', 'Кольорова гармонія — найважливіше, на мою думку.'),
]

for title, uname, text in comments_data:
    try:
        article = Article.objects.get(title=title)
        author = admin if uname == 'admin' else users.get(uname)
        if not Comment.objects.filter(article=article, text=text).exists():
            Comment.objects.create(article=article, author=author, text=text)
            print(f"✓ Comment by {uname} on '{title[:30]}...'")
    except Article.DoesNotExist:
        pass

print("\n✅ Демо-дані успішно створено!")
print("─────────────────────────────────────")
print("👤 admin    / admin123  (superuser)")
print("👤 nazar    / pass1234")
print("👤 olena    / pass1234")
print("─────────────────────────────────────")
print("🌐 http://127.0.0.1:8000/")
print("🔧 http://127.0.0.1:8000/admin/")
