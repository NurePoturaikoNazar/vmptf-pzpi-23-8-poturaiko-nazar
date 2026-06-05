const express = require('express');
const db = require('../db');
const { authMiddleware } = require('../middleware/auth');

const router = express.Router();

// PATCH /api/lectures/:id/complete — toggle lecture completion
router.patch('/:id/complete', authMiddleware, (req, res) => {
  try {
    const lectureId = req.params.id;

    // Check lecture exists
    const lecture = db.prepare('SELECT * FROM lectures WHERE id = ?').get(lectureId);
    if (!lecture) {
      return res.status(404).json({ error: 'Лекцію не знайдено' });
    }

    // Check user is enrolled in this course
    const enrollment = db.prepare(
      'SELECT id FROM enrollments WHERE user_id = ? AND course_id = ?'
    ).get(req.user.id, lecture.course_id);

    if (!enrollment) {
      return res.status(400).json({ error: 'Ви не записані на цей курс' });
    }

    // Toggle progress
    const progress = db.prepare(
      'SELECT * FROM lecture_progress WHERE user_id = ? AND lecture_id = ?'
    ).get(req.user.id, lectureId);

    if (progress) {
      const newStatus = progress.completed ? 0 : 1;
      db.prepare('UPDATE lecture_progress SET completed = ? WHERE id = ?').run(newStatus, progress.id);
      res.json({ completed: newStatus });
    } else {
      db.prepare(
        'INSERT INTO lecture_progress (user_id, lecture_id, completed) VALUES (?, ?, 1)'
      ).run(req.user.id, lectureId);
      res.json({ completed: 1 });
    }
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка сервера' });
  }
});

module.exports = router;
