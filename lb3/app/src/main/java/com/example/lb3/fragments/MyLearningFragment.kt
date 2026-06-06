package com.example.lb3.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lb3.R
import com.example.lb3.activities.CourseDetailsActivity
import com.example.lb3.activities.MainActivity
import com.example.lb3.adapters.MyCourseAdapter
import com.example.lb3.data.MockDataRepository
import com.example.lb3.data.PreferencesManager
import com.example.lb3.network.NetworkClient
import org.json.JSONObject

class MyLearningFragment : Fragment() {

    private lateinit var prefs: PreferencesManager
    private lateinit var adapter: MyCourseAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_learning, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager.getInstance(requireContext())

        val user = prefs.getCurrentUser()
        if (user == null) return

        val recyclerView = view.findViewById<RecyclerView>(R.id.learning_recycler)
        adapter = MyCourseAdapter { course ->
            startActivity(CourseDetailsActivity.intent(requireContext(), course.id))
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        refreshList(user.id, view)

        view.findViewById<View>(R.id.browse_button).setOnClickListener {
            (activity as? MainActivity)?.navigateToCatalog()
        }
    }

    override fun onResume() {
        super.onResume()
        prefs.getCurrentUser()?.let { refreshList(it.id, requireView()) }
    }

    private fun refreshList(userId: Int, view: View) {
        val authUser = prefs.getAuthUser()
        if (authUser != null) {
            // fetch from server
            NetworkClient.getEnrollments(onSuccess = { list ->
                val items = list.mapNotNull { obj ->
                    try {
                        val enrollmentId = obj.optInt("enrollment_id")
                        val courseObj = obj
                        val cid = courseObj.optInt("id")
                        val title = courseObj.optString("title")
                        val desc = courseObj.optString("description")
                        val duration = courseObj.optString("duration")
                        val price = courseObj.optInt("price")
                        val teacherId = courseObj.optInt("teacher_id")
                        val total = courseObj.optInt("total_lectures", 0)
                        val completed = courseObj.optInt("completed_lectures", 0)
                        val completedFlag = courseObj.optInt("course_completed", 0)
                        val course = com.example.lb3.models.Course(cid, title, desc, desc, duration, price, teacherId, 0)
                        MyCourseAdapter.EnrollmentView(enrollmentId, course, completedFlag, completed, total)
                    } catch (e: Exception) {
                        null
                    }
                }
                adapter.submitList(items)
                view.findViewById<LinearLayout>(R.id.empty_state).visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }, onError = { _ ->
                view.findViewById<LinearLayout>(R.id.empty_state).visibility = View.VISIBLE
            })
        } else {
            val enrolled = prefs.getEnrolledCourses(userId)
            val courses = enrolled.mapNotNull { MockDataRepository.getCourse(it.first) }
            // convert to EnrollmentView with local progress
            val items = courses.map { course ->
                val status = prefs.getCourseStatus(userId, course.id)
                val completedFlag = if (status == com.example.lb3.models.CourseStatus.COMPLETED) 1 else 0
                val progress = prefs.getLessonProgress(userId, course.id)
                MyCourseAdapter.EnrollmentView(0, course, completedFlag, (progress * (MockDataRepository.getLessonsForCourse(course.id).size) / 100), MockDataRepository.getLessonsForCourse(course.id).size)
            }
            adapter.submitList(items)
            view.findViewById<LinearLayout>(R.id.empty_state).visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
