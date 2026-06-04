from django.urls import path
from . import views

app_name = 'blog'

urlpatterns = [
    # Home
    path('', views.home, name='home'),

    # Categories
    path('categories/', views.category_list, name='category_list'),
    path('categories/create/', views.category_create, name='category_create'),
    path('categories/<int:pk>/', views.category_detail, name='category_detail'),

    # Articles
    path('articles/', views.article_list, name='article_list'),
    path('articles/create/', views.article_create, name='article_create'),
    path('articles/<int:pk>/', views.article_detail, name='article_detail'),
    path('articles/<int:pk>/edit/', views.article_edit, name='article_edit'),
    path('articles/<int:pk>/delete/', views.article_delete, name='article_delete'),

    # Comments
    path('comments/<int:pk>/delete/', views.comment_delete, name='comment_delete'),

    # Auth
    path('register/', views.register_view, name='register'),
    path('login/', views.login_view, name='login'),
    path('logout/', views.logout_view, name='logout'),
]
