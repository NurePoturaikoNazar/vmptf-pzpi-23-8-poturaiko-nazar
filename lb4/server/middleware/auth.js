const jwt = require('jsonwebtoken');

const JWT_SECRET = 'spekulant_secret_key_2024';

function authMiddleware(req, res, next) {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Необхідна авторизація' });
  }

  const token = authHeader.split(' ')[1];

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded;
    next();
  } catch (err) {
    return res.status(401).json({ error: 'Невалідний токен' });
  }
}

function adminMiddleware(req, res, next) {
  if (!req.user || req.user.role !== 'admin') {
    return res.status(403).json({ error: 'Доступ лише для адміністратора' });
  }
  next();
}

module.exports = { authMiddleware, adminMiddleware, JWT_SECRET };
