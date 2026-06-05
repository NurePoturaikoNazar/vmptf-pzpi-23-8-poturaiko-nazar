const express = require('express');
const db = require('../db');
const { authMiddleware } = require('../middleware/auth');

const router = express.Router();

// POST /api/enrollments — enroll in a course
router.post('/', authMiddleware, (req, res) => {
  try {
    const { course_id } = req.body;

    if (!course_id) {
      return res.status(400).json({ error: 'course_id обов\'язковий' });
    }

    // Check course exists
    const course = db.prepare('SELECT id FROM courses WHERE id = ?').get(course_id);
    if (!course) {
      return res.status(404).json({ error: 'Курс не знайдено' });
    }

    // Check if already enrolled
    const existing = db.prepare(
      'SELECT id FROM enrollments WHERE user_id = ? AND course_id = ?'
    ).get(req.user.id, course_id);

    if (existing) {
      return res.status(400).json({ error: 'Ви вже записані на цей курс' });
    }

    const result = db.prepare(
      'INSERT INTO enrollments (user_id, course_id) VALUES (?, ?)'
    ).run(req.user.id, course_id);

    // Initialize lecture progress for all lectures in this course
    const lectures = db.prepare('SELECT id FROM lectures WHERE course_id = ?').all(course_id);
    const insertProgress = db.prepare(
      'INSERT INTO lecture_progress (user_id, lecture_id, completed) VALUES (?, ?, 0)'
    );

    for (const lecture of lectures) {
      insertProgress.run(req.user.id, lecture.id);
    }

    res.status(201).json({ id: result.lastInsertRowid, message: 'Успішно записано на курс' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка сервера' });
  }
});

// GET /api/enrollments — get user's enrolled courses
router.get('/', authMiddleware, (req, res) => {
  try {
    const enrollments = db.prepare(`
      SELECT 
        e.id as enrollment_id,
        e.completed as course_completed,
        e.enrolled_at,
        c.*,
        t.name as teacher_name,
        t.avatar as teacher_avatar,
        COALESCE(AVG(r.rating), 0) as avg_rating,
        (SELECT COUNT(*) FROM lectures WHERE course_id = c.id) as total_lectures,
        (SELECT COUNT(*) FROM lecture_progress lp
         JOIN lectures l ON lp.lecture_id = l.id
         WHERE lp.user_id = ? AND l.course_id = c.id AND lp.completed = 1) as completed_lectures
      FROM enrollments e
      JOIN courses c ON e.course_id = c.id
      LEFT JOIN teachers t ON c.teacher_id = t.id
      LEFT JOIN reviews r ON c.id = r.course_id
      WHERE e.user_id = ?
      GROUP BY c.id
      ORDER BY e.enrolled_at DESC
    `).all(req.user.id, req.user.id);

    res.json(enrollments);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка сервера' });
  }
});

// PATCH /api/enrollments/:id/complete — mark course completed
router.patch('/:id/complete', authMiddleware, (req, res) => {
  try {
    const enrollment = db.prepare(
      'SELECT * FROM enrollments WHERE id = ? AND user_id = ?'
    ).get(req.params.id, req.user.id);

    if (!enrollment) {
      return res.status(404).json({ error: 'Запис не знайдено' });
    }

    const newStatus = enrollment.completed ? 0 : 1;
    db.prepare('UPDATE enrollments SET completed = ? WHERE id = ?').run(newStatus, req.params.id);

    res.json({ completed: newStatus });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка сервера' });
  }
});

module.exports = router;
