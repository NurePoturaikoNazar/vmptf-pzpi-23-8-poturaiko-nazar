from django.contrib import admin
from .models import Category, Article, Comment


@admin.register(Category)
class CategoryAdmin(admin.ModelAdmin):
    list_display = ['name', 'article_count']
    search_fields = ['name']

    def article_count(self, obj):
        return obj.articles.count()
    article_count.short_description = 'Кількість статей'


@admin.register(Article)
class ArticleAdmin(admin.ModelAdmin):
    list_display = ['title', 'author', 'category', 'published_at', 'comment_count']
    list_filter = ['category', 'published_at']
    search_fields = ['title', 'content', 'author__username']
    date_hierarchy = 'published_at'
    raw_id_fields = ['author']

    def comment_count(self, obj):
        return obj.comments.count()
    comment_count.short_description = 'Коментарів'


@admin.register(Comment)
class CommentAdmin(admin.ModelAdmin):
    list_display = ['get_display_author', 'article', 'created_at', 'text_preview']
    list_filter = ['created_at']
    search_fields = ['text', 'author__username', 'author_name']

    def text_preview(self, obj):
        return obj.text[:60] + '...' if len(obj.text) > 60 else obj.text
    text_preview.short_description = 'Текст'
