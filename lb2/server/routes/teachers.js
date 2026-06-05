const express = require('express');
const db = require('../db');

const router = express.Router();

// GET /api/teachers — all teachers
router.get('/', (req, res) => {
  try {
    const teachers = db.prepare(`
      SELECT 
        t.*,
        COUNT(c.id) as course_count
      FROM teachers t
      LEFT JOIN courses c ON t.id = c.teacher_id
      GROUP BY t.id
      ORDER BY t.name
    `).all();

    res.json(teachers);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка сервера' });
  }
});

// GET /api/teachers/:id — teacher with their courses
router.get('/:id', (req, res) => {
  try {
    const teacher = db.prepare('SELECT * FROM teachers WHERE id = ?').get(req.params.id);

    if (!teacher) {
      return res.status(404).json({ error: 'Викладача не знайдено' });
    }

    const courses = db.prepare(`
      SELECT 
        c.*,
        COALESCE(AVG(r.rating), 0) as avg_rating,
        COUNT(r.id) as review_count
      FROM courses c
      LEFT JOIN reviews r ON c.id = r.course_id
      WHERE c.teacher_id = ?
      GROUP BY c.id
    `).all(req.params.id);

    res.json({ ...teacher, courses });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка сервера' });
  }
});

module.exports = router;
