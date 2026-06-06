package com.example.lb3.network

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.lb3.data.PreferencesManager
import com.example.lb3.models.Course
import com.example.lb3.models.Lesson
import com.example.lb3.models.User
import com.example.lb3.models.Review
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object NetworkClient {
    private const val BASE_URL = "http://10.0.2.2:5000/api"
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun authToken(): String? {
        return appContext?.let { PreferencesManager.getInstance(it).getAuthToken() }
    }

    private fun openConnection(path: String, method: String = "GET"): HttpURLConnection {
        val url = URL("$BASE_URL$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.connectTimeout = 7000
        conn.readTimeout = 7000
        conn.doInput = true
        if (method == "POST" || method == "PATCH") conn.doOutput = true
        val token = authToken()
        if (!token.isNullOrEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer $token")
        }
        conn.setRequestProperty("Content-Type", "application/json")
        return conn
    }

    // --- Auth ---
    fun login(email: String, password: String, onSuccess: (String, User) -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                val conn = openConnection("/login", "POST")
                val body = JSONObject().put("email", email).put("password", password).toString()
                OutputStreamWriter(conn.outputStream).use { it.write(body) }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream.bufferedReader().use { it.readText() } else conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (code !in 200..299) throw Exception("HTTP $code: $stream")

                val obj = JSONObject(stream)
                val token = obj.getString("token")
                val userObj = obj.getJSONObject("user")
                val user = User(userObj.getInt("id"), userObj.getString("name"), userObj.getString("email"), null)

                // persist
                appContext?.let { PreferencesManager.getInstance(it).setAuthToken(token); PreferencesManager.getInstance(it).setAuthUser(user) }

                Handler(Looper.getMainLooper()).post { onSuccess(token, user) }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { onError(e.message ?: "Unknown error") }
            }
        }.start()
    }

    fun register(name: String, email: String, password: String, onSuccess: (String, User) -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                val conn = openConnection("/register", "POST")
                val body = JSONObject().put("name", name).put("email", email).put("password", password).toString()
                OutputStreamWriter(conn.outputStream).use { it.write(body) }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream.bufferedReader().use { it.readText() } else conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (code !in 200..299) throw Exception("HTTP $code: $stream")

                val obj = JSONObject(stream)
                val token = obj.getString("token")
                val userObj = obj.getJSONObject("user")
                val user = User(userObj.getInt("id"), userObj.getString("name"), userObj.getString("email"), null)

                appContext?.let { PreferencesManager.getInstance(it).setAuthToken(token); PreferencesManager.getInstance(it).setAuthUser(user) }

                Handler(Looper.getMainLooper()).post { onSuccess(token, user) }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { onError(e.message ?: "Unknown error") }
            }
        }.start()
    }

    // --- Courses ---
    fun fetchCourses(onSuccess: (List<Course>) -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                val conn = openConnection("/courses", "GET")
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream.bufferedReader().use { it.readText() } else conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (code !in 200..299) throw Exception("HTTP $code: $stream")

                val arr = JSONArray(stream)
                val list = mutableListOf<Course>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.optInt("id")
                    val title = obj.optString("title")
                    val fullDescription = obj.optString("description")
                    val shortDescription = if (fullDescription.length > 80) fullDescription.substring(0, 80) + "..." else fullDescription
                    val duration = obj.optString("duration")
                    val price = obj.optInt("price")
                    val teacherId = obj.optInt("teacher_id", obj.optInt("teacherId"))
                    list.add(Course(id, title, shortDescription, fullDescription, duration, price, teacherId, 0))
                }

                Handler(Looper.getMainLooper()).post { onSuccess(list) }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { onError(e.message ?: "Unknown error") }
            }
        }.start()
    }

    fun fetchCourseDetails(courseId: Int, onSuccess: (Course, List<Lesson>, Int?, Int, Map<Int, Int>, Boolean, List<Review>) -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                val conn = openConnection("/courses/$courseId", "GET")
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream.bufferedReader().use { it.readText() } else conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (code !in 200..299) throw Exception("HTTP $code: $stream")

                val obj = JSONObject(stream)
                val id = obj.optInt("id")
                val title = obj.optString("title")
                val fullDescription = obj.optString("description")
                val shortDescription = if (fullDescription.length > 80) fullDescription.substring(0, 80) + "..." else fullDescription
                val duration = obj.optString("duration")
                val price = obj.optInt("price")
                val teacherId = obj.optInt("teacher_id", obj.optInt("teacherId"))

                val course = Course(id, title, shortDescription, fullDescription, duration, price, teacherId, 0)

                val lessonsArr = obj.optJSONArray("lectures") ?: JSONArray()
                val lessons = mutableListOf<Lesson>()
                for (i in 0 until lessonsArr.length()) {
                    val l = lessonsArr.getJSONObject(i)
                    val lid = l.optInt("id")
                    val ltitle = l.optString("title")
                    val orderNum = l.optInt("order_num", l.optInt("orderNum"))
                    val videoUrl = l.optString("video_url", "https://example.com/lesson/$lid")
                    lessons.add(Lesson(lid, course.id, ltitle, orderNum, videoUrl))
                }

                // enrollment and lecture progress
                val enrollmentObj = obj.optJSONObject("enrollment")
                val enrollmentId: Int? = enrollmentObj?.optInt("id")
                val enrollmentCompleted: Int = enrollmentObj?.optInt("completed") ?: 0

                val lectureProgressMap = mutableMapOf<Int, Int>()
                val lpArr = obj.optJSONArray("lectureProgress") ?: JSONArray()
                for (i in 0 until lpArr.length()) {
                    val lp = lpArr.getJSONObject(i)
                    lectureProgressMap[lp.optInt("lecture_id")] = lp.optInt("completed")
                }

                val userHasReviewed = obj.optBoolean("user_has_reviewed", false)

                // parse reviews
                val reviewsArr = obj.optJSONArray("reviews") ?: JSONArray()
                val reviews = mutableListOf<Review>()
                for (i in 0 until reviewsArr.length()) {
                    val r = reviewsArr.getJSONObject(i)
                    val rid = r.optInt("id")
                    val ruserId = r.optInt("user_id", r.optInt("userId"))
                    val rcourseId = r.optInt("course_id", r.optInt("courseId"))
                    val ruserName = r.optString("user_name", r.optString("userName"))
                    val rrating = r.optInt("rating")
                    val rcomment = r.optString("comment", "")
                    val rcreated = r.optLong("created_at", System.currentTimeMillis())
                    reviews.add(Review(rid, ruserId, rcourseId, ruserName, rrating, rcomment, rcreated))
                }

                Handler(Looper.getMainLooper()).post { onSuccess(course, lessons, enrollmentId, enrollmentCompleted, lectureProgressMap, userHasReviewed, reviews) }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { onError(e.message ?: "Unknown error") }
            }
        }.start()
    }

    // --- Enroll / Enrollments ---
    fun enroll(courseId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                val conn = openConnection("/enrollments", "POST")
                val body = JSONObject().put("course_id", courseId).toString()
                OutputStreamWriter(conn.outputStream).use { it.write(body) }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream.bufferedReader().use { it.readText() } else conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (code !in 200..299) throw Exception("HTTP $code: $stream")
                Handler(Looper.getMainLooper()).post { onSuccess() }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { onError(e.message ?: "Unknown error") }
            }
        }.start()
    }

    fun getEnrollments(onSuccess: (List<JSONObject>) -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                val conn = openConnection("/enrollments", "GET")
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream.bufferedReader().use { it.readText() } else conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (code !in 200..299) throw Exception("HTTP $code: $stream")
                val arr = JSONArray(stream)
                val list = mutableListOf<JSONObject>()
                for (i in 0 until arr.length()) list.add(arr.getJSONObject(i))
                Handler(Looper.getMainLooper()).post { onSuccess(list) }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { onError(e.message ?: "Unknown error") }
            }
        }.start()
    }

    // Toggle enrollment complete status
    fun toggleEnrollmentComplete(enrollmentId: Int, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                val conn = openConnection("/enrollments/$enrollmentId/complete", "PATCH")
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream.bufferedReader().use { it.readText() } else conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (code !in 200..299) throw Exception("HTTP $code: $stream")
                val obj = JSONObject(stream)
                val completed = obj.optInt("completed")
                Handler(Looper.getMainLooper()).post { onSuccess(completed) }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { onError(e.message ?: "Unknown error") }
            }
        }.start()
    }

    // --- Lecture progress ---
    fun toggleLectureComplete(lectureId: Int, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                val conn = openConnection("/lectures/$lectureId/complete", "PATCH")
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream.bufferedReader().use { it.readText() } else conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (code !in 200..299) throw Exception("HTTP $code: $stream")
                val obj = JSONObject(stream)
                val completed = obj.optInt("completed")
                Handler(Looper.getMainLooper()).post { onSuccess(completed) }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { onError(e.message ?: "Unknown error") }
            }
        }.start()
    }

    // --- Reviews ---
    fun addReview(courseId: Int, rating: Int, comment: String, onSuccess: (JSONObject) -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                val conn = openConnection("/reviews", "POST")
                val body = JSONObject().put("course_id", courseId).put("rating", rating).put("comment", comment).toString()
                OutputStreamWriter(conn.outputStream).use { it.write(body) }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream.bufferedReader().use { it.readText() } else conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (code !in 200..299) throw Exception("HTTP $code: $stream")
                val obj = JSONObject(stream)
                Handler(Looper.getMainLooper()).post { onSuccess(obj) }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { onError(e.message ?: "Unknown error") }
            }
        }.start()
    }
}
