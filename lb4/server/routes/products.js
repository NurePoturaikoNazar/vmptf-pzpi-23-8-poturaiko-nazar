const express = require('express');
const { Op } = require('sequelize');
const { Product, Category } = require('../models');
const { authMiddleware, adminMiddleware } = require('../middleware/auth');

const router = express.Router();

router.get('/', async (req, res) => {
  try {
    const { search, categoryId } = req.query;
    const where = {};

    if (search) {
      where.name = { [Op.like]: `%${search}%` };
    }

    if (categoryId) {
      where.categoryId = categoryId;
    }

    const products = await Product.findAll({
      where,
      include: [{ model: Category, as: 'category', attributes: ['id', 'name'] }],
      order: [['name', 'ASC']],
    });

    res.json(products);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка отримання товарів' });
  }
});

router.get('/:id', async (req, res) => {
  try {
    const product = await Product.findByPk(req.params.id, {
      include: [{ model: Category, as: 'category', attributes: ['id', 'name'] }],
    });

    if (!product) {
      return res.status(404).json({ error: 'Товар не знайдено' });
    }

    res.json(product);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка отримання товару' });
  }
});

router.post('/', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const { name, price, description, categoryId } = req.body;

    if (!name || price == null || !categoryId) {
      return res.status(400).json({ error: 'Заповніть обов\'язкові поля' });
    }

    const category = await Category.findByPk(categoryId);
    if (!category) {
      return res.status(400).json({ error: 'Категорію не знайдено' });
    }

    const product = await Product.create({
      name,
      price,
      description: description || '',
      categoryId,
    });

    const created = await Product.findByPk(product.id, {
      include: [{ model: Category, as: 'category', attributes: ['id', 'name'] }],
    });

    res.status(201).json(created);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка створення товару' });
  }
});

router.put('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const product = await Product.findByPk(req.params.id);
    if (!product) {
      return res.status(404).json({ error: 'Товар не знайдено' });
    }

    const { name, price, description, categoryId } = req.body;

    if (categoryId) {
      const category = await Category.findByPk(categoryId);
      if (!category) {
        return res.status(400).json({ error: 'Категорію не знайдено' });
      }
    }

    await product.update({
      name: name ?? product.name,
      price: price ?? product.price,
      description: description ?? product.description,
      categoryId: categoryId ?? product.categoryId,
    });

    const updated = await Product.findByPk(product.id, {
      include: [{ model: Category, as: 'category', attributes: ['id', 'name'] }],
    });

    res.json(updated);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка оновлення товару' });
  }
});

router.delete('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const product = await Product.findByPk(req.params.id);
    if (!product) {
      return res.status(404).json({ error: 'Товар не знайдено' });
    }

    await product.destroy();
    res.json({ message: 'Товар видалено' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка видалення товару' });
  }
});

module.exports = router;
