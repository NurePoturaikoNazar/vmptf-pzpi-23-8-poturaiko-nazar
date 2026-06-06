package com.example.lb3.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lb3.R
import com.example.lb3.adapters.LessonAdapter
import com.example.lb3.adapters.ReviewAdapter
import com.example.lb3.adapters.TeacherAdapter
import com.example.lb3.data.MockDataRepository
import com.example.lb3.network.NetworkClient
import com.example.lb3.models.Lesson
import com.example.lb3.models.Course
import com.example.lb3.data.PreferencesManager
import com.example.lb3.models.CourseStatus
import com.example.lb3.utils.StarRatingHelper
import com.example.lb3.utils.UiUtils

class CourseDetailsActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager
    private var courseId: Int = 0
    private var selectedRating = 5
    private lateinit var lessonAdapter: LessonAdapter
    private lateinit var reviewAdapter: ReviewAdapter
    private var loadedCourse: Course? = null
    private var remoteLessons: List<Lesson> = emptyList()
    private var remoteEnrollmentId: Int? = null
    private var remoteEnrollmentCompleted: Int = 0
    private var lectureProgressMap: Map<Int, Int> = emptyMap()
    private var remoteUserHasReviewed: Boolean = false
    private var remoteReviews: List<com.example.lb3.models.Review> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager.getInstance(this)
        courseId = intent.getIntExtra(EXTRA_COURSE_ID, -1)

        enableEdgeToEdge()
        setContentView(R.layout.activity_course_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.details_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        NetworkClient.init(applicationContext)

        // Load course details from backend; fallback to local mock data on error
        NetworkClient.fetchCourseDetails(courseId, onSuccess = { course, lessons, enrollmentId, enrollmentCompleted, lpMap, userHasReviewed, reviews ->
            loadedCourse = course
            remoteLessons = lessons
            remoteEnrollmentId = enrollmentId
            remoteEnrollmentCompleted = enrollmentCompleted
            lectureProgressMap = lpMap
            remoteUserHasReviewed = userHasReviewed
            remoteReviews = reviews
            bindCourse(course)
            setupTeachers(course.teacherId)
            setupLessons()
            setupReviews()
        }, onError = { err ->
            val course = MockDataRepository.getCourse(courseId)
            if (course == null) {
                finish()
                return@fetchCourseDetails
            }
            loadedCourse = course
            remoteLessons = MockDataRepository.getLessonsForCourse(courseId)
            bindCourse(course)
            setupTeachers(course.teacherId)
            setupLessons()
            setupReviews()
        })
    }

    override fun onResume() {
        super.onResume()
        loadedCourse?.let { bindCourse(it) }
        setupLessons()
        setupReviews()
    }

    private fun bindCourse(course: com.example.lb3.models.Course) {
        val authUser = prefs.getAuthUser()
        val localUser = prefs.getCurrentUser()
        val user = authUser ?: localUser

        val status = if (authUser != null) {
            if (remoteEnrollmentId != null) {
                if (remoteEnrollmentCompleted == 1) CourseStatus.COMPLETED else CourseStatus.IN_PROGRESS
            } else {
                CourseStatus.AVAILABLE
            }
        } else if (localUser != null) {
            prefs.getCourseStatus(localUser.id, course.id)
        } else {
            CourseStatus.AVAILABLE
        }

        findViewById<View>(R.id.hero_image)
            .setBackgroundColor(UiUtils.headerColor(this, course.headerColorIndex))

        findViewById<TextView>(R.id.back_button).setOnClickListener { finish() }
        findViewById<TextView>(R.id.course_title).text = course.title
        findViewById<TextView>(R.id.course_duration).text = "⏱ ${course.duration}"
        findViewById<TextView>(R.id.course_price).text = getString(R.string.price_format, course.price)
        findViewById<TextView>(R.id.course_description).text = course.fullDescription

        val avgRating = if (remoteReviews.isNotEmpty()) {
            remoteReviews.map { it.rating }.average().toFloat()
        } else prefs.getAverageRating(course.id)
        StarRatingHelper.buildStars(
            this,
            findViewById(R.id.star_rating),
            avgRating
        )

        val statusBadge = findViewById<TextView>(R.id.status_badge)
        UiUtils.applyStatusBadge(this, statusBadge, status)

        val enrollButton = findViewById<TextView>(R.id.enroll_button)
        val completeButton = findViewById<TextView>(R.id.complete_button)

        when (status) {
            CourseStatus.AVAILABLE -> {
                enrollButton.visibility = View.VISIBLE
                enrollButton.text = getString(R.string.enroll)
                enrollButton.setOnClickListener {
                    if (user == null) {
                        Toast.makeText(this, R.string.login_required, Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (authUser != null) {
                        NetworkClient.enroll(course.id, onSuccess = {
                            Toast.makeText(this, R.string.enrolled, Toast.LENGTH_SHORT).show()
                            // refresh details
                            NetworkClient.fetchCourseDetails(courseId, onSuccess = { c, lessons, enrollmentId, enrollmentCompleted, lpMap, userHasReviewed, reviews ->
                                loadedCourse = c
                                remoteLessons = lessons
                                remoteEnrollmentId = enrollmentId
                                remoteEnrollmentCompleted = enrollmentCompleted
                                lectureProgressMap = lpMap
                                remoteUserHasReviewed = userHasReviewed
                                remoteReviews = reviews
                                bindCourse(c)
                                setupLessons()
                                setupReviews()
                            }, onError = { _ -> })
                        }, onError = { err -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show() })
                    } else {
                        prefs.enroll(user.id, course.id)
                        Toast.makeText(this, R.string.enrolled, Toast.LENGTH_SHORT).show()
                        bindCourse(course)
                        setupLessons()
                        setupReviews()
                    }
                }
                completeButton.visibility = View.GONE
            }
            CourseStatus.IN_PROGRESS -> {
                enrollButton.visibility = View.VISIBLE
                enrollButton.text = getString(R.string.enrolled)
                enrollButton.alpha = 0.7f
                enrollButton.setOnClickListener(null)
                completeButton.visibility = View.VISIBLE
                completeButton.setOnClickListener {
                    user?.let {
                        if (authUser != null && remoteEnrollmentId != null) {
                            NetworkClient.toggleEnrollmentComplete(remoteEnrollmentId!!, onSuccess = { newStatus ->
                                remoteEnrollmentCompleted = newStatus
                                Toast.makeText(this, R.string.course_completed, Toast.LENGTH_SHORT).show()
                                bindCourse(course)
                                setupReviews()
                            }, onError = { err -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show() })
                        } else {
                            prefs.completeCourse(it.id, course.id)
                            Toast.makeText(this, R.string.course_completed, Toast.LENGTH_SHORT).show()
                            bindCourse(course)
                            setupReviews()
                        }
                    }
                }
            }
            CourseStatus.COMPLETED -> {
                enrollButton.visibility = View.VISIBLE
                enrollButton.text = getString(R.string.course_completed)
                enrollButton.setBackgroundResource(R.drawable.bg_badge_completed)
                enrollButton.setTextColor(getColor(R.color.success_text))
                enrollButton.setOnClickListener(null)
                completeButton.visibility = View.GONE
            }
        }
    }

    private fun setupTeachers(teacherId: Int) {
        val teacher = MockDataRepository.getTeacher(teacherId) ?: return
        val recycler = findViewById<RecyclerView>(R.id.teachers_recycler)
        val adapter = TeacherAdapter(compact = true)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        adapter.submitList(listOf(teacher))
    }

    private fun setupLessons() {
        val authUser = prefs.getAuthUser()
        val localUser = prefs.getCurrentUser()
        val currentUser = authUser ?: localUser
        val status = if (authUser != null) {
            if (remoteEnrollmentId != null) {
                if (remoteEnrollmentCompleted == 1) CourseStatus.COMPLETED else CourseStatus.IN_PROGRESS
            } else {
                CourseStatus.AVAILABLE
            }
        } else localUser?.let { prefs.getCourseStatus(it.id, courseId) } ?: CourseStatus.AVAILABLE
        val showActions = status == CourseStatus.IN_PROGRESS || status == CourseStatus.COMPLETED

        if (!::lessonAdapter.isInitialized) {
            lessonAdapter = LessonAdapter(
                prefs = prefs,
                userId = currentUser?.id,
                showActions = showActions,
                onToggle = { lesson ->
                    val authUser2 = prefs.getAuthUser()
                    val localUser2 = prefs.getCurrentUser()
                    if (authUser2 != null) {
                        NetworkClient.toggleLectureComplete(lesson.id, onSuccess = { newStatus ->
                            lectureProgressMap = lectureProgressMap.toMutableMap().also { it[lesson.id] = newStatus }
                            lessonAdapter.notifyDataSetChanged()
                        }, onError = { err -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show() })
                    } else if (localUser2 != null) {
                        lessonAdapter.showActions = true
                        prefs.toggleLesson(localUser2.id, lesson.id)
                        lessonAdapter.notifyDataSetChanged()
                    }
                }
            )
            findViewById<RecyclerView>(R.id.lessons_recycler).apply {
                layoutManager = LinearLayoutManager(this@CourseDetailsActivity)
                adapter = lessonAdapter
            }
        } else {
            lessonAdapter.showActions = showActions
        }

        val lessonsToShow = if (remoteLessons.isNotEmpty()) remoteLessons else MockDataRepository.getLessonsForCourse(courseId)
        lessonAdapter.submitList(lessonsToShow)
    }

    private fun setupReviews() {
        val authUser2 = prefs.getAuthUser()
        val localUser2 = prefs.getCurrentUser()
        val reviews = if (authUser2 != null) remoteReviews else prefs.getReviewsForCourse(courseId)
        val status2 = if (authUser2 != null) {
            if (remoteEnrollmentId != null) {
                if (remoteEnrollmentCompleted == 1) CourseStatus.COMPLETED else CourseStatus.IN_PROGRESS
            } else CourseStatus.AVAILABLE
        } else localUser2?.let { prefs.getCourseStatus(it.id, courseId) } ?: CourseStatus.AVAILABLE

        findViewById<TextView>(R.id.reviews_title).text = getString(R.string.reviews_count, reviews.size)

        if (!::reviewAdapter.isInitialized) {
            reviewAdapter = ReviewAdapter()
            findViewById<RecyclerView>(R.id.reviews_recycler).apply {
                layoutManager = LinearLayoutManager(this@CourseDetailsActivity)
                adapter = reviewAdapter
            }
        }
        reviewAdapter.submitList(reviews)

        val noReviews = findViewById<TextView>(R.id.no_reviews_text)
        noReviews.visibility = if (reviews.isEmpty()) View.VISIBLE else View.GONE

        val reviewForm = findViewById<LinearLayout>(R.id.review_form)
        val reviewHint = findViewById<TextView>(R.id.review_hint_text)
        val ratingInput = findViewById<LinearLayout>(R.id.rating_input)
        val reviewInput = findViewById<EditText>(R.id.review_input)

        val canSubmit = if (authUser2 != null) (remoteEnrollmentId != null && remoteEnrollmentCompleted == 1 && !remoteUserHasReviewed) else (localUser2 != null && status2 == CourseStatus.COMPLETED && !prefs.hasUserReviewed(localUser2.id, courseId))

        if (canSubmit) {
            reviewForm.visibility = View.VISIBLE
            reviewHint.visibility = View.GONE
            refreshRatingInput(ratingInput)
            findViewById<Button>(R.id.submit_review_button).setOnClickListener {
                val comment = reviewInput.text.toString().trim()
                if (comment.isEmpty()) return@setOnClickListener
                if (authUser2 != null) {
                    NetworkClient.addReview(courseId, selectedRating, comment, onSuccess = { obj ->
                        // server returns { review, avg_rating, review_count }
                        val newReview = obj.optJSONObject("review")
                        newReview?.let {
                            val rid = it.optInt("id")
                            val ruserId = it.optInt("user_id")
                            val rcourseId = it.optInt("course_id")
                            val ruserName = it.optString("user_name", it.optString("userName"))
                            val rrating = it.optInt("rating")
                            val rcomment = it.optString("comment", "")
                            val rcreated = it.optLong("created_at", System.currentTimeMillis())
                            remoteReviews = listOf(com.example.lb3.models.Review(rid, ruserId, rcourseId, ruserName, rrating, rcomment, rcreated)) + remoteReviews
                            reviewInput.text.clear()
                            selectedRating = 5
                            setupReviews()
                            Toast.makeText(this, R.string.submit_review, Toast.LENGTH_SHORT).show()
                        }
                    }, onError = { err -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show() })
                } else if (localUser2 != null) {
                    prefs.addReview(localUser2.id, localUser2.name, courseId, selectedRating, comment)
                    reviewInput.text.clear()
                    selectedRating = 5
                    setupReviews()
                    Toast.makeText(this, R.string.submit_review, Toast.LENGTH_SHORT).show()
                }
            }
        } else if ((authUser2 != null && remoteEnrollmentId != null && remoteEnrollmentCompleted != 1) || (localUser2 != null && status2 != CourseStatus.COMPLETED)) {
            reviewForm.visibility = View.GONE
            reviewHint.visibility = View.VISIBLE
        } else {
            reviewForm.visibility = View.GONE
            reviewHint.visibility = View.GONE
        }
    }

    private fun refreshRatingInput(container: LinearLayout) {
        StarRatingHelper.buildStars(this, container, selectedRating.toFloat(), true) { rating ->
            selectedRating = rating
            refreshRatingInput(container)
        }
    }

    companion object {
        private const val EXTRA_COURSE_ID = "course_id"

        fun intent(context: Context, courseId: Int): Intent {
            return Intent(context, CourseDetailsActivity::class.java).apply {
                putExtra(EXTRA_COURSE_ID, courseId)
            }
        }
    }
}
