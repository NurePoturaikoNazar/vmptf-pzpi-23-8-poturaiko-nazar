const express = require('express');
const cors = require('cors');
require('./db');

const authRoutes = require('./routes/auth');
const courseRoutes = require('./routes/courses');
const enrollmentRoutes = require('./routes/enrollments');
const reviewRoutes = require('./routes/reviews');
const lectureRoutes = require('./routes/lectures');
const teacherRoutes = require('./routes/teachers');

const app = express();
const PORT = 5000;

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api', authRoutes);
app.use('/api/courses', courseRoutes);
app.use('/api/enrollments', enrollmentRoutes);
app.use('/api/reviews', reviewRoutes);
app.use('/api/lectures', lectureRoutes);
app.use('/api/teachers', teacherRoutes);

// Health check
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', message: 'SkillUp API is running' });
});

app.listen(PORT, () => {
  console.log(`SkillUp server running on http://localhost:${PORT}`);
});
