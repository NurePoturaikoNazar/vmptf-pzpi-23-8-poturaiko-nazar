const express = require('express');
const cors = require('cors');
const { sequelize } = require('./models');
const seed = require('./seed');

const authRoutes = require('./routes/auth');
const categoryRoutes = require('./routes/categories');
const productRoutes = require('./routes/products');
const orderRoutes = require('./routes/orders');

const app = express();
const PORT = 5001;

app.use(cors());
app.use(express.json());

app.use('/api', authRoutes);
app.use('/api/categories', categoryRoutes);
app.use('/api/products', productRoutes);
app.use('/api/orders', orderRoutes);

app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', message: 'спекулянт.юа API працює' });
});

async function start() {
  try {
    await sequelize.sync({ force: false });
    await seed();
    app.listen(PORT, () => {
      console.log(`спекулянт.юа server running on http://localhost:${PORT}`);
    });
  } catch (err) {
    console.error('Failed to start server:', err);
    process.exit(1);
  }
}

start();
