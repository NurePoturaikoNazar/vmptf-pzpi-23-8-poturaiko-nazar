package com.example.lb3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lb3.R
import com.example.lb3.data.PreferencesManager
import com.example.lb3.models.Course
import com.example.lb3.models.CourseStatus
import com.example.lb3.utils.UiUtils

class CourseAdapter(
    private val prefs: PreferencesManager,
    private val userId: Int?,
    private val onCourseClick: (Course) -> Unit,
    private val onEnrollClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    private var courses: List<Course> = emptyList()

    fun submitList(list: List<Course>) {
        courses = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position])
    }

    override fun getItemCount(): Int = courses.size

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val header: View = itemView.findViewById(R.id.course_header)
        private val price: TextView = itemView.findViewById(R.id.course_price)
        private val statusBadge: TextView = itemView.findViewById(R.id.status_badge)
        private val title: TextView = itemView.findViewById(R.id.course_title)
        private val description: TextView = itemView.findViewById(R.id.course_description)
        private val duration: TextView = itemView.findViewById(R.id.course_duration)
        private val rating: TextView = itemView.findViewById(R.id.course_rating)
        private val enrollButton: TextView = itemView.findViewById(R.id.enroll_button)

        fun bind(course: Course) {
            val context = itemView.context
            header.setBackgroundColor(UiUtils.headerColor(context, course.headerColorIndex))
            price.text = context.getString(R.string.price_format, course.price)
            title.text = course.title
            description.text = course.shortDescription
            duration.text = "⏱ ${course.duration}"

            val avgRating = prefs.getAverageRating(course.id)
            rating.text = if (avgRating > 0) String.format("%.1f", avgRating) else "—"

            val status = if (userId != null) {
                prefs.getCourseStatus(userId, course.id)
            } else {
                CourseStatus.AVAILABLE
            }
            UiUtils.applyStatusBadge(context, statusBadge, status)

            when (status) {
                CourseStatus.AVAILABLE -> {
                    enrollButton.visibility = View.VISIBLE
                    enrollButton.text = context.getString(R.string.enroll)
                    enrollButton.alpha = 1f
                    enrollButton.setOnClickListener { onEnrollClick(course) }
                }
                CourseStatus.IN_PROGRESS, CourseStatus.COMPLETED -> {
                    enrollButton.visibility = View.VISIBLE
                    enrollButton.text = context.getString(R.string.enrolled)
                    enrollButton.alpha = 0.7f
                    enrollButton.setOnClickListener(null)
                }
            }

            itemView.setOnClickListener { onCourseClick(course) }
        }
    }
}
