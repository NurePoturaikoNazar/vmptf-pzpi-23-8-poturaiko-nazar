const express = require('express');
const db = require('../db');
const { optionalAuth } = require('../middleware/auth');

const router = express.Router();

// GET /api/courses — all courses with avg rating & teacher
router.get('/', optionalAuth, (req, res) => {
  try {
    const courses = db.prepare(`
      SELECT 
        c.*,
        t.name as teacher_name,
        t.avatar as teacher_avatar,
        t.specialty as teacher_specialty,
        COALESCE(AVG(r.rating), 0) as avg_rating,
        COUNT(r.id) as review_count
      FROM courses c
      LEFT JOIN teachers t ON c.teacher_id = t.id
      LEFT JOIN reviews r ON c.id = r.course_id
      GROUP BY c.id
      ORDER BY c.id
    `).all();

    // If user is authenticated, add enrollment status
    if (req.user) {
      const enrollments = db.prepare(
        'SELECT course_id FROM enrollments WHERE user_id = ?'
      ).all(req.user.id);
      const enrolledIds = new Set(enrollments.map((e) => e.course_id));

      courses.forEach((c) => {
        c.is_enrolled = enrolledIds.has(c.id);
      });
    }

    res.json(courses);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка сервера' });
  }
});

// GET /api/courses/:id — single course with lectures, teacher, reviews
router.get('/:id', optionalAuth, (req, res) => {
  try {
    const course = db.prepare(`
      SELECT 
        c.*,
        t.id as teacher_id,
        t.name as teacher_name,
        t.bio as teacher_bio,
        t.avatar as teacher_avatar,
        t.specialty as teacher_specialty,
        COALESCE(AVG(r.rating), 0) as avg_rating,
        COUNT(r.id) as review_count
      FROM courses c
      LEFT JOIN teachers t ON c.teacher_id = t.id
      LEFT JOIN reviews r ON c.id = r.course_id
      WHERE c.id = ?
      GROUP BY c.id
    `).get(req.params.id);

    if (!course) {
      return res.status(404).json({ error: 'Курс не знайдено' });
    }

    // Lectures
    const lectures = db.prepare(
      'SELECT * FROM lectures WHERE course_id = ? ORDER BY order_num'
    ).all(req.params.id);

    // Reviews with user names
    const reviews = db.prepare(`
      SELECT r.*, u.name as user_name
      FROM reviews r
      JOIN users u ON r.user_id = u.id
      WHERE r.course_id = ?
      ORDER BY r.created_at DESC
    `).all(req.params.id);

    // Enrollment & lecture progress if authenticated
    let enrollment = null;
    let lectureProgress = [];
    if (req.user) {
      enrollment = db.prepare(
        'SELECT * FROM enrollments WHERE user_id = ? AND course_id = ?'
      ).get(req.user.id, req.params.id);

      if (enrollment) {
        lectureProgress = db.prepare(`
          SELECT lecture_id, completed FROM lecture_progress
          WHERE user_id = ? AND lecture_id IN (
            SELECT id FROM lectures WHERE course_id = ?
          )
        `).all(req.user.id, req.params.id);
      }

      // Check if user already reviewed
      const userReview = db.prepare(
        'SELECT id FROM reviews WHERE user_id = ? AND course_id = ?'
      ).get(req.user.id, req.params.id);
      course.user_has_reviewed = !!userReview;
    }

    res.json({
      ...course,
      lectures,
      reviews,
      enrollment,
      lectureProgress,
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка сервера' });
  }
});

module.exports = router;
