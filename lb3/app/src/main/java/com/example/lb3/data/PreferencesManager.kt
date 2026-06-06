package com.example.lb3.data

import android.content.Context
import com.example.lb3.models.CourseStatus
import com.example.lb3.models.Review
import com.example.lb3.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // Gson keys for server auth
    private val AUTH_USER_KEY = "auth_user"
    private val AUTH_TOKEN_KEY = "auth_token"

    fun ensureDemoUser() {
        val users = getUsers()
        if (users.none { it.email == DEMO_EMAIL }) {
            val demo = User(1, "Demo User", DEMO_EMAIL, DEMO_PASSWORD)
            saveUsers(listOf(demo))
        }
    }

    // --- Server auth helpers ---
    fun setAuthToken(token: String?) {
        prefs.edit().apply {
            if (token == null) remove(AUTH_TOKEN_KEY) else putString(AUTH_TOKEN_KEY, token)
        }.apply()
    }

    fun getAuthToken(): String? = prefs.getString(AUTH_TOKEN_KEY, null)

    fun setAuthUser(user: User?) {
        prefs.edit().apply {
            if (user == null) remove(AUTH_USER_KEY) else putString(AUTH_USER_KEY, gson.toJson(user))
        }.apply()
    }

    fun getAuthUser(): User? {
        val json = prefs.getString(AUTH_USER_KEY, null) ?: return null
        return gson.fromJson(json, User::class.java)
    }

    fun registerUser(name: String, email: String, password: String): User? {
        val users = getUsers().toMutableList()
        if (users.any { it.email.equals(email, ignoreCase = true) }) return null
        val newId = (users.maxOfOrNull { it.id } ?: 0) + 1
        val user = User(newId, name, email.trim().lowercase(), password)
        users.add(user)
        saveUsers(users)
        return user
    }

    fun login(email: String, password: String): User? {
        return getUsers().find {
            it.email.equals(email.trim(), ignoreCase = true) && it.password == password
        }
    }

    fun getCurrentUser(): User? {
        val userId = prefs.getInt(KEY_CURRENT_USER_ID, -1)
        if (userId == -1) return null
        return getUsers().find { it.id == userId }
    }

    fun setCurrentUser(user: User?) {
        prefs.edit().apply {
            if (user == null) {
                remove(KEY_CURRENT_USER_ID)
            } else {
                putInt(KEY_CURRENT_USER_ID, user.id)
            }
        }.apply()
    }

    fun logout() {
        setCurrentUser(null)
        setAuthToken(null)
        setAuthUser(null)
    }

    fun isLoggedIn(): Boolean = getCurrentUser() != null || getAuthToken() != null

    fun getCourseStatus(userId: Int, courseId: Int): CourseStatus {
        val key = enrollmentKey(userId, courseId)
        val value = prefs.getString(key, null) ?: return CourseStatus.AVAILABLE
        return CourseStatus.valueOf(value)
    }

    fun enroll(userId: Int, courseId: Int) {
        prefs.edit().putString(enrollmentKey(userId, courseId), CourseStatus.IN_PROGRESS.name).apply()
    }

    fun completeCourse(userId: Int, courseId: Int) {
        prefs.edit().putString(enrollmentKey(userId, courseId), CourseStatus.COMPLETED.name).apply()
    }

    fun getEnrolledCourses(userId: Int): List<Pair<Int, CourseStatus>> {
        return MockDataRepository.courses.mapNotNull { course ->
            val status = getCourseStatus(userId, course.id)
            if (status == CourseStatus.IN_PROGRESS || status == CourseStatus.COMPLETED) {
                course.id to status
            } else null
        }
    }

    fun isLessonCompleted(userId: Int, lessonId: Int): Boolean {
        return prefs.getBoolean(lessonKey(userId, lessonId), false)
    }

    fun toggleLesson(userId: Int, lessonId: Int): Boolean {
        val newValue = !isLessonCompleted(userId, lessonId)
        prefs.edit().putBoolean(lessonKey(userId, lessonId), newValue).apply()
        return newValue
    }

    fun getLessonProgress(userId: Int, courseId: Int): Int {
        val total = MockDataRepository.getLessonsForCourse(courseId).size
        if (total == 0) return 0
        val completed = MockDataRepository.getLessonsForCourse(courseId)
            .count { isLessonCompleted(userId, it.id) }
        return (completed * 100) / total
    }

    fun getReviewsForCourse(courseId: Int): List<Review> {
        return getAllReviews().filter { it.courseId == courseId }.sortedByDescending { it.createdAt }
    }

    fun hasUserReviewed(userId: Int, courseId: Int): Boolean {
        return getAllReviews().any { it.userId == userId && it.courseId == courseId }
    }

    fun addReview(userId: Int, userName: String, courseId: Int, rating: Int, comment: String): Review {
        val reviews = getAllReviews().toMutableList()
        val newId = (reviews.maxOfOrNull { it.id } ?: 0) + 1
        val review = Review(newId, userId, courseId, userName, rating, comment.trim(), System.currentTimeMillis())
        reviews.add(review)
        saveReviews(reviews)
        return review
    }

    fun getAverageRating(courseId: Int): Float {
        val reviews = getReviewsForCourse(courseId)
        if (reviews.isEmpty()) return 0f
        return reviews.map { it.rating }.average().toFloat()
    }

    fun getCompletedCount(userId: Int): Int {
        return getEnrolledCourses(userId).count { it.second == CourseStatus.COMPLETED }
    }

    fun getEnrolledCount(userId: Int): Int {
        return getEnrolledCourses(userId).size
    }

    private fun getUsers(): List<User> {
        val json = prefs.getString(KEY_USERS, null) ?: return emptyList()
        val type = object : TypeToken<List<User>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveUsers(users: List<User>) {
        prefs.edit().putString(KEY_USERS, gson.toJson(users)).apply()
    }

    private fun getAllReviews(): List<Review> {
        val json = prefs.getString(KEY_REVIEWS, null) ?: return emptyList()
        val type = object : TypeToken<List<Review>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveReviews(reviews: List<Review>) {
        prefs.edit().putString(KEY_REVIEWS, gson.toJson(reviews)).apply()
    }

    private fun enrollmentKey(userId: Int, courseId: Int) = "enrollment_${userId}_$courseId"

    private fun lessonKey(userId: Int, lessonId: Int) = "lesson_${userId}_$lessonId"

    companion object {
        private const val PREFS_NAME = "lb3_prefs"
        private const val KEY_USERS = "users"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_REVIEWS = "reviews"
        const val DEMO_EMAIL = "demo@lb3.com"
        const val DEMO_PASSWORD = "password123"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also {
                    it.ensureDemoUser()
                    instance = it
                }
            }
        }
    }
}
