const bcrypt = require('bcryptjs');
const { sequelize, User, Category, Product } = require('./models');

async function seed() {
  const adminExists = await User.findOne({ where: { email: 'admin@spekulant.ua' } });
  if (!adminExists) {
    const hash = await bcrypt.hash('admin123', 10);
    await User.create({
      username: 'admin',
      email: 'admin@spekulant.ua',
      password: hash,
      role: 'admin',
    });
    console.log('Admin created: admin@spekulant.ua / admin123');
  }

  const categoryCount = await Category.count();
  if (categoryCount === 0) {
    const categories = await Category.bulkCreate([
      { name: 'Електроніка' },
      { name: 'Одяг' },
      { name: 'Книги' },
      { name: 'Спорт' },
    ]);

    await Product.bulkCreate([
      {
        name: 'Смартфон Galaxy X',
        price: 24999.99,
        description: 'Потужний смартфон з AMOLED-екраном та потрійною камерою.',
        categoryId: categories[0].id,
      },
      {
        name: 'Навушники Pro Sound',
        price: 3499.00,
        description: 'Бездротові навушники з активним шумозаглушенням.',
        categoryId: categories[0].id,
      },
      {
        name: 'Куртка Urban Wind',
        price: 2899.50,
        description: 'Легка вітровка для міського стилю.',
        categoryId: categories[1].id,
      },
      {
        name: 'Кросівки Sprint Run',
        price: 4199.00,
        description: 'Бігові кросівки з амортизацією для щоденних тренувань.',
        categoryId: categories[3].id,
      },
      {
        name: 'Книга «Алгоритми»',
        price: 899.00,
        description: 'Практичний посібник з алгоритмів та структур даних.',
        categoryId: categories[2].id,
      },
      {
        name: 'Планшет Tab 11',
        price: 15999.00,
        description: '11-дюймовий планшет для роботи та навчання.',
        categoryId: categories[0].id,
      },
    ]);
    console.log('Categories and products seeded');
  }
}

module.exports = seed;
