const Database = require('better-sqlite3');
const path = require('path');
const bcrypt = require('bcryptjs');

const dbPath = path.join(__dirname, 'skillup.db');
const db = new Database(dbPath);

// Enable WAL mode for better performance
db.pragma('journal_mode = WAL');
db.pragma('foreign_keys = ON');

// ── Create Tables ──────────────────────────────────────────────

db.exec(`
  CREATE TABLE IF NOT EXISTS teachers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    bio TEXT,
    avatar TEXT,
    specialty TEXT
  );

  CREATE TABLE IF NOT EXISTS courses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    duration TEXT,
    price REAL NOT NULL DEFAULT 0,
    image TEXT,
    teacher_id INTEGER,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id)
  );

  CREATE TABLE IF NOT EXISTS lectures (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    order_num INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
  );

  CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS enrollments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    completed INTEGER NOT NULL DEFAULT 0,
    enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE(user_id, course_id)
  );

  CREATE TABLE IF NOT EXISTS lecture_progress (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    lecture_id INTEGER NOT NULL,
    completed INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE CASCADE,
    UNIQUE(user_id, lecture_id)
  );

  CREATE TABLE IF NOT EXISTS reviews (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    rating INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE(user_id, course_id)
  );
`);

// ── Seed Data ──────────────────────────────────────────────────

const teacherCount = db.prepare('SELECT COUNT(*) as count FROM teachers').get().count;

if (teacherCount === 0) {
  console.log('Seeding database...');

  // Teachers
  const insertTeacher = db.prepare(
    'INSERT INTO teachers (name, bio, avatar, specialty) VALUES (?, ?, ?, ?)'
  );

  const teachers = [
    {
      name: 'Олена Коваленко',
      bio: 'Full-stack розробниця з 10-річним досвідом. Працювала в Google та Amazon. Спеціалізується на React та Node.js.',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Olena',
      specialty: 'Web Development',
    },
    {
      name: 'Андрій Мельник',
      bio: 'Data Scientist з PhD у машинному навчанні. Автор курсів з Python та аналізу даних.',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Andriy',
      specialty: 'Data Science',
    },
    {
      name: 'Марія Шевченко',
      bio: 'UX/UI дизайнерка з портфоліо у сфері фінтех та електронної комерції. Викладає дизайн уже 7 років.',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Maria',
      specialty: 'Design',
    },
  ];

  const teacherIds = teachers.map((t) =>
    insertTeacher.run(t.name, t.bio, t.avatar, t.specialty).lastInsertRowid
  );

  // Courses
  const insertCourse = db.prepare(
    'INSERT INTO courses (title, description, duration, price, image, teacher_id) VALUES (?, ?, ?, ?, ?, ?)'
  );

  const courses = [
    {
      title: 'React для початківців',
      description:
        'Повний курс з React.js — від основ до створення реальних застосунків. Ви навчитесь працювати з компонентами, хуками, роутингом та станом.',
      duration: '24 години',
      price: 1299,
      image: 'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=600&h=400&fit=crop',
      teacher_id: teacherIds[0],
    },
    {
      title: 'Node.js та Express',
      description:
        'Створення серверних додатків на Node.js з фреймворком Express. REST API, робота з базами даних, аутентифікація.',
      duration: '20 годин',
      price: 1099,
      image: 'https://images.unsplash.com/photo-1627398242454-45a1465c2479?w=600&h=400&fit=crop',
      teacher_id: teacherIds[0],
    },
    {
      title: 'Python для Data Science',
      description:
        'Аналіз даних, візуалізація та машинне навчання з Python. Pandas, NumPy, Matplotlib, Scikit-learn.',
      duration: '30 годин',
      price: 1599,
      image: 'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=600&h=400&fit=crop',
      teacher_id: teacherIds[1],
    },
    {
      title: 'Основи Machine Learning',
      description:
        'Введення в машинне навчання: регресія, класифікація, кластеризація та нейронні мережі.',
      duration: '36 годин',
      price: 1899,
      image: 'https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=600&h=400&fit=crop',
      teacher_id: teacherIds[1],
    },
    {
      title: 'UI/UX Дизайн з Figma',
      description:
        'Проектування інтерфейсів у Figma: wireframes, прототипи, дизайн-системи та робота з командою.',
      duration: '18 годин',
      price: 999,
      image: 'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=600&h=400&fit=crop',
      teacher_id: teacherIds[2],
    },
    {
      title: 'Веб-дизайн: від ідеї до продукту',
      description:
        'Повний процес створення вебсайту: аналіз ЦА, мудборди, створення макетів та адаптивний дизайн.',
      duration: '22 години',
      price: 1199,
      image: 'https://images.unsplash.com/photo-1547658719-da2b51169166?w=600&h=400&fit=crop',
      teacher_id: teacherIds[2],
    },
  ];

  const courseIds = courses.map((c) =>
    insertCourse.run(c.title, c.description, c.duration, c.price, c.image, c.teacher_id)
      .lastInsertRowid
  );

  // Lectures
  const insertLecture = db.prepare(
    'INSERT INTO lectures (course_id, title, order_num) VALUES (?, ?, ?)'
  );

  const lecturesByCourse = [
    // React course
    ['Вступ до React', 'JSX та компоненти', 'Props та State', 'Хуки: useState та useEffect', 'React Router', 'Робота з API'],
    // Node.js course
    ['Основи Node.js', 'Модулі та NPM', 'Express.js фреймворк', 'REST API', 'Middleware', 'Аутентифікація'],
    // Python DS
    ['Основи Python', 'NumPy', 'Pandas', 'Візуалізація з Matplotlib', 'Статистичний аналіз', 'Введення в ML'],
    // ML course
    ['Що таке ML', 'Лінійна регресія', 'Класифікація', 'Дерева рішень', 'Кластеризація', 'Нейронні мережі', 'Проект'],
    // UI/UX
    ['Основи UX', 'User Research', 'Wireframing', 'Прототипування у Figma', 'Дизайн-системи'],
    // Web Design
    ['Аналіз цільової аудиторії', 'Мудборди', 'Типографіка та колір', 'Створення макету', 'Адаптивний дизайн', 'Фінальний проект'],
  ];

  lecturesByCourse.forEach((lectures, courseIndex) => {
    lectures.forEach((title, lectureIndex) => {
      insertLecture.run(courseIds[courseIndex], title, lectureIndex + 1);
    });
  });

  // Demo user
  const hash = bcrypt.hashSync('password123', 10);
  db.prepare('INSERT INTO users (name, email, password) VALUES (?, ?, ?)').run(
    'Demo User',
    'demo@skillup.com',
    hash
  );

  console.log('Database seeded successfully!');
}

module.exports = db;
