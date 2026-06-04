from django.shortcuts import render, get_object_or_404, redirect
from django.contrib.auth import login, logout, authenticate
from django.contrib.auth.decorators import login_required
from django.contrib import messages
from django.db.models import Count, Q
from django.http import HttpResponseForbidden
from django.views.decorators.http import require_POST

from .models import Article, Category, Comment
from .forms import ArticleForm, CommentForm, CategoryForm, RegisterForm


# ── Home ──────────────────────────────────────────────────────────────────────
def home(request):
    articles = Article.objects.select_related('author', 'category').annotate(
        num_comments=Count('comments')
    ).order_by('-published_at')[:6]
    categories = Category.objects.annotate(num_articles=Count('articles'))[:8]
    context = {
        'articles': articles,
        'categories': categories,
    }
    return render(request, 'blog/home.html', context)


# ── Category views ────────────────────────────────────────────────────────────
def category_list(request):
    categories = Category.objects.annotate(num_articles=Count('articles')).order_by('name')
    return render(request, 'blog/category_list.html', {'categories': categories})


def category_detail(request, pk):
    category = get_object_or_404(Category, pk=pk)
    articles = Article.objects.filter(category=category).select_related('author').annotate(
        num_comments=Count('comments')
    ).order_by('-published_at')
    return render(request, 'blog/category_detail.html', {
        'category': category,
        'articles': articles,
    })


@login_required
def category_create(request):
    if not request.user.is_staff:
        return HttpResponseForbidden('Тільки адміністратори можуть створювати категорії.')
    if request.method == 'POST':
        form = CategoryForm(request.POST)
        if form.is_valid():
            form.save()
            messages.success(request, 'Категорію успішно створено!')
            return redirect('blog:category_list')
    else:
        form = CategoryForm()
    return render(request, 'blog/category_form.html', {'form': form, 'title': 'Нова категорія'})


# ── Article views ─────────────────────────────────────────────────────────────
def article_list(request):
    sort = request.GET.get('sort', 'date')
    articles = Article.objects.select_related('author', 'category').annotate(
        num_comments=Count('comments')
    )
    if sort == 'comments':
        articles = articles.order_by('-num_comments', '-published_at')
    else:
        articles = articles.order_by('-published_at')

    return render(request, 'blog/article_list.html', {
        'articles': articles,
        'current_sort': sort,
    })


def article_detail(request, pk):
    article = get_object_or_404(
        Article.objects.select_related('author', 'category').prefetch_related('comments__author'),
        pk=pk
    )
    comments = article.comments.select_related('author').order_by('created_at')
    comment_form = CommentForm()

    if request.method == 'POST':
        if not request.user.is_authenticated:
            messages.error(request, 'Увійдіть, щоб залишати коментарі.')
            return redirect('blog:login')
        comment_form = CommentForm(request.POST)
        if comment_form.is_valid():
            comment = comment_form.save(commit=False)
            comment.article = article
            comment.author = request.user
            comment.save()
            messages.success(request, 'Коментар додано!')
            return redirect('blog:article_detail', pk=pk)

    can_edit = (
        request.user.is_authenticated and
        (request.user == article.author or request.user.is_staff)
    )
    return render(request, 'blog/article_detail.html', {
        'article': article,
        'comments': comments,
        'comment_form': comment_form,
        'can_edit': can_edit,
    })


@login_required
def article_create(request):
    if request.method == 'POST':
        form = ArticleForm(request.POST)
        if form.is_valid():
            article = form.save(commit=False)
            article.author = request.user
            article.save()
            messages.success(request, 'Статтю успішно створено!')
            return redirect('blog:article_detail', pk=article.pk)
    else:
        form = ArticleForm()
    return render(request, 'blog/article_form.html', {'form': form, 'title': 'Нова стаття'})


@login_required
def article_edit(request, pk):
    article = get_object_or_404(Article, pk=pk)
    if request.user != article.author and not request.user.is_staff:
        return HttpResponseForbidden('У вас немає прав для редагування цієї статті.')
    if request.method == 'POST':
        form = ArticleForm(request.POST, instance=article)
        if form.is_valid():
            form.save()
            messages.success(request, 'Статтю оновлено!')
            return redirect('blog:article_detail', pk=pk)
    else:
        form = ArticleForm(instance=article)
    return render(request, 'blog/article_form.html', {
        'form': form,
        'title': 'Редагувати статтю',
        'article': article,
    })


@login_required
def article_delete(request, pk):
    article = get_object_or_404(Article, pk=pk)
    if request.user != article.author and not request.user.is_staff:
        return HttpResponseForbidden('У вас немає прав для видалення цієї статті.')
    if request.method == 'POST':
        article.delete()
        messages.success(request, 'Статтю видалено.')
        return redirect('blog:article_list')
    return render(request, 'blog/article_confirm_delete.html', {'article': article})


# ── Comment views ─────────────────────────────────────────────────────────────
@login_required
@require_POST
def comment_delete(request, pk):
    comment = get_object_or_404(Comment, pk=pk)
    article_pk = comment.article.pk
    if request.user != comment.author and not request.user.is_staff:
        return HttpResponseForbidden('Ви не можете видалити цей коментар.')
    comment.delete()
    messages.success(request, 'Коментар видалено.')
    return redirect('blog:article_detail', pk=article_pk)


# ── Auth views ────────────────────────────────────────────────────────────────
def register_view(request):
    if request.user.is_authenticated:
        return redirect('blog:home')
    if request.method == 'POST':
        form = RegisterForm(request.POST)
        if form.is_valid():
            user = form.save()
            login(request, user)
            messages.success(request, f'Ласкаво просимо, {user.username}!')
            return redirect('blog:home')
    else:
        form = RegisterForm()
    return render(request, 'blog/register.html', {'form': form})


def login_view(request):
    if request.user.is_authenticated:
        return redirect('blog:home')
    if request.method == 'POST':
        username = request.POST.get('username')
        password = request.POST.get('password')
        user = authenticate(request, username=username, password=password)
        if user:
            login(request, user)
            messages.success(request, f'Ласкаво просимо, {user.username}!')
            next_url = request.GET.get('next', 'blog:home')
            return redirect(next_url)
        else:
            messages.error(request, 'Невірний логін або пароль.')
    return render(request, 'blog/login.html', {})


def logout_view(request):
    logout(request)
    messages.info(request, 'Ви вийшли з системи.')
    return redirect('blog:home')
