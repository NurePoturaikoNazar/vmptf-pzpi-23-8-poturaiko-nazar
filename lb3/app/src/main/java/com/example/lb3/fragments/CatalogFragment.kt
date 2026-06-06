package com.example.lb3.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lb3.R
import com.example.lb3.activities.CourseDetailsActivity
import com.example.lb3.adapters.CourseAdapter
import com.example.lb3.data.MockDataRepository
import com.example.lb3.network.NetworkClient
import com.example.lb3.models.Course
import com.example.lb3.data.PreferencesManager

class CatalogFragment : Fragment() {

    private lateinit var prefs: PreferencesManager
    private lateinit var adapter: CourseAdapter
    private var allCourses: List<Course> = listOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_catalog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager.getInstance(requireContext())

        setupRecycler(view)
        loadCourses()
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    private fun loadCourses() {
        NetworkClient.fetchCourses(onSuccess = { courses ->
            allCourses = courses
            adapter.submitList(allCourses)
        }, onError = { err ->
            // fallback to local mock data
            allCourses = MockDataRepository.courses
            adapter.submitList(allCourses)
        })
    }

    private fun setupRecycler(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.courses_recycler)
        adapter = CourseAdapter(
            prefs = prefs,
            userId = prefs.getCurrentUser()?.id,
            onCourseClick = { course ->
                startActivity(CourseDetailsActivity.intent(requireContext(), course.id))
            },
            onEnrollClick = { course ->
                val user = prefs.getCurrentUser()
                if (user == null) {
                    Toast.makeText(context, R.string.login_required, Toast.LENGTH_SHORT).show()
                } else {
                    prefs.enroll(user.id, course.id)
                    Toast.makeText(context, R.string.enrolled, Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged()
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.submitList(allCourses)
    }
}
