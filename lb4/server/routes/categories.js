const express = require('express');
const { Category } = require('../models');

const router = express.Router();

router.get('/', async (req, res) => {
  try {
    const categories = await Category.findAll({
      order: [['name', 'ASC']],
    });
    res.json(categories);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка отримання категорій' });
  }
});

module.exports = router;
