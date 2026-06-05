const express = require('express');
const db = require('../db');
const { authMiddleware } = require('../middleware/auth');

const router = express.Router();

// POST /api/reviews — add a review
router.post('/', authMiddleware, (req, res) => {
  try {
    const { course_id, rating, comment } = req.body;

    if (!course_id || !rating) {
      return res.status(400).json({ error: 'course_id та rating обов\'язкові' });
    }

    if (rating < 1 || rating > 5) {
      return res.status(400).json({ error: 'Рейтинг має бути від 1 до 5' });
    }

    // Check course exists
    const course = db.prepare('SELECT id FROM courses WHERE id = ?').get(course_id);
    if (!course) {
      return res.status(404).json({ error: 'Курс не знайдено' });
    }

    // Check if user is enrolled
    const enrollment = db.prepare(
      'SELECT id FROM enrollments WHERE user_id = ? AND course_id = ?'
    ).get(req.user.id, course_id);

    if (!enrollment) {
      return res.status(400).json({ error: 'Ви маєте бути записані на курс для написання відгуку' });
    }

    // Check if already reviewed
    const existing = db.prepare(
      'SELECT id FROM reviews WHERE user_id = ? AND course_id = ?'
    ).get(req.user.id, course_id);

    if (existing) {
      return res.status(400).json({ error: 'Ви вже залишили відгук на цей курс' });
    }

    const result = db.prepare(
      'INSERT INTO reviews (user_id, course_id, rating, comment) VALUES (?, ?, ?, ?)'
    ).run(req.user.id, course_id, rating, comment || '');

    // Get the created review with user name
    const review = db.prepare(`
      SELECT r.*, u.name as user_name
      FROM reviews r
      JOIN users u ON r.user_id = u.id
      WHERE r.id = ?
    `).get(result.lastInsertRowid);

    // Calc new avg
    const stats = db.prepare(
      'SELECT AVG(rating) as avg_rating, COUNT(*) as review_count FROM reviews WHERE course_id = ?'
    ).get(course_id);

    res.status(201).json({
      review,
      avg_rating: stats.avg_rating,
      review_count: stats.review_count,
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка сервера' });
  }
});

module.exports = router;
