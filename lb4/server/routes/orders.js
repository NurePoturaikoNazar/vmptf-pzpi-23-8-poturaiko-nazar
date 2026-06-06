const express = require('express');
const { Order, OrderItem, Product, User } = require('../models');
const { authMiddleware } = require('../middleware/auth');

const router = express.Router();

router.get('/', authMiddleware, async (req, res) => {
  try {
    const where = req.user.role === 'admin' ? {} : { userId: req.user.id };

    const orders = await Order.findAll({
      where,
      include: [
        {
          model: User,
          as: 'user',
          attributes: ['id', 'username', 'email'],
        },
        {
          model: Product,
          as: 'products',
          through: { attributes: ['quantity', 'price'] },
        },
      ],
      order: [['createdAt', 'DESC']],
    });

    res.json(orders);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка отримання замовлень' });
  }
});

router.get('/:id', authMiddleware, async (req, res) => {
  try {
    const order = await Order.findByPk(req.params.id, {
      include: [
        {
          model: User,
          as: 'user',
          attributes: ['id', 'username', 'email'],
        },
        {
          model: Product,
          as: 'products',
          through: { attributes: ['quantity', 'price'] },
        },
      ],
    });

    if (!order) {
      return res.status(404).json({ error: 'Замовлення не знайдено' });
    }

    if (req.user.role !== 'admin' && order.userId !== req.user.id) {
      return res.status(403).json({ error: 'Немає доступу до цього замовлення' });
    }

    res.json(order);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка отримання замовлення' });
  }
});

router.post('/', authMiddleware, async (req, res) => {
  try {
    const { items } = req.body;

    if (!items || !Array.isArray(items) || items.length === 0) {
      return res.status(400).json({ error: 'Додайте товари до замовлення' });
    }

    let totalAmount = 0;
    const orderItemsData = [];

    for (const item of items) {
      const product = await Product.findByPk(item.productId);
      if (!product) {
        return res.status(400).json({ error: `Товар #${item.productId} не знайдено` });
      }

      const quantity = item.quantity || 1;
      const price = parseFloat(product.price);
      totalAmount += price * quantity;

      orderItemsData.push({
        productId: product.id,
        quantity,
        price,
      });
    }

    const order = await Order.create({
      userId: req.user.id,
      totalAmount,
      status: 'pending',
    });

    for (const item of orderItemsData) {
      await OrderItem.create({
        orderId: order.id,
        ...item,
      });
    }

    const created = await Order.findByPk(order.id, {
      include: [
        {
          model: User,
          as: 'user',
          attributes: ['id', 'username', 'email'],
        },
        {
          model: Product,
          as: 'products',
          through: { attributes: ['quantity', 'price'] },
        },
      ],
    });

    res.status(201).json(created);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Помилка створення замовлення' });
  }
});

module.exports = router;
