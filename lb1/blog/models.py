from django.db import models
from django.contrib.auth.models import User
from django.urls import reverse


class Category(models.Model):
    name = models.CharField(max_length=200, verbose_name='Назва')
    description = models.TextField(blank=True, verbose_name='Опис')

    class Meta:
        verbose_name = 'Категорія'
        verbose_name_plural = 'Категорії'
        ordering = ['name']

    def __str__(self):
        return self.name

    def get_absolute_url(self):
        return reverse('blog:category_detail', kwargs={'pk': self.pk})


class Article(models.Model):
    title = models.CharField(max_length=300, verbose_name='Заголовок')
    content = models.TextField(verbose_name='Текст')
    published_at = models.DateTimeField(auto_now_add=True, verbose_name='Дата публікації')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='Дата оновлення')
    category = models.ForeignKey(
        Category,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name='articles',
        verbose_name='Категорія'
    )
    author = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        related_name='articles',
        verbose_name='Автор'
    )

    class Meta:
        verbose_name = 'Стаття'
        verbose_name_plural = 'Статті'
        ordering = ['-published_at']

    def __str__(self):
        return self.title

    def get_absolute_url(self):
        return reverse('blog:article_detail', kwargs={'pk': self.pk})

    def comment_count(self):
        return self.comments.count()


class Comment(models.Model):
    article = models.ForeignKey(
        Article,
        on_delete=models.CASCADE,
        related_name='comments',
        verbose_name='Стаття'
    )
    author = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        related_name='comments',
        verbose_name='Автор',
        null=True,
        blank=True
    )
    author_name = models.CharField(max_length=100, blank=True, verbose_name='Ім\'я автора')
    text = models.TextField(verbose_name='Текст коментаря')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='Дата створення')

    class Meta:
        verbose_name = 'Коментар'
        verbose_name_plural = 'Коментарі'
        ordering = ['created_at']

    def __str__(self):
        name = self.author.username if self.author else self.author_name
        return f'Коментар від {name} до "{self.article.title}"'

    def get_display_author(self):
        if self.author:
            return self.author.username
        return self.author_name or 'Анонім'
