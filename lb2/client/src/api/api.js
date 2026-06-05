const API_BASE = '/api';

function getToken() {
  return localStorage.getItem('skillup_token');
}

async function request(endpoint, options = {}) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(data.error || 'Помилка запиту');
  }

  return data;
}

export const api = {
  getCourses: () => request('/courses'),
  getCourse: (id) => request(`/courses/${id}`),
  getTeachers: () => request('/teachers'),
  getTeacher: (id) => request(`/teachers/${id}`),

  register: (body) =>
    request('/register', { method: 'POST', body: JSON.stringify(body) }),
  login: (body) =>
    request('/login', { method: 'POST', body: JSON.stringify(body) }),
  getMe: () => request('/me'),

  enroll: (courseId) =>
    request('/enrollments', {
      method: 'POST',
      body: JSON.stringify({ course_id: courseId }),
    }),
  getEnrollments: () => request('/enrollments'),
  toggleCourseComplete: (enrollmentId) =>
    request(`/enrollments/${enrollmentId}/complete`, { method: 'PATCH' }),

  addReview: (body) =>
    request('/reviews', { method: 'POST', body: JSON.stringify(body) }),
  toggleLectureComplete: (lectureId) =>
    request(`/lectures/${lectureId}/complete`, { method: 'PATCH' }),
};
