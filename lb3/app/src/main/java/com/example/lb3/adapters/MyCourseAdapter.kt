package com.example.lb3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lb3.R
import com.example.lb3.models.Course
import com.example.lb3.utils.UiUtils

class MyCourseAdapter(
    private val onCourseClick: (Course) -> Unit
) : RecyclerView.Adapter<MyCourseAdapter.MyCourseViewHolder>() {

    data class EnrollmentView(
        val enrollmentId: Int,
        val course: Course,
        val completedFlag: Int,
        val completedLectures: Int,
        val totalLectures: Int
    )

    private var items: List<EnrollmentView> = emptyList()

    fun submitList(list: List<EnrollmentView>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_course, parent, false)
        return MyCourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyCourseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MyCourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val header: View = itemView.findViewById(R.id.course_thumb)
        private val title: TextView = itemView.findViewById(R.id.course_title)
        private val statusBadge: TextView = itemView.findViewById(R.id.status_badge)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        private val progressText: TextView = itemView.findViewById(R.id.progress_text)

        fun bind(ev: EnrollmentView) {
            val context = itemView.context
            val course = ev.course
            header.setBackgroundColor(UiUtils.headerColor(context, course.headerColorIndex))
            title.text = course.title

            val progress = if (ev.totalLectures == 0) 0 else (ev.completedLectures * 100) / ev.totalLectures
            progressBar.progress = progress
            progressText.text = "$progress%"

            // status badge text
            val statusText = if (ev.completedFlag == 1) context.getString(R.string.course_completed) else context.getString(R.string.enrolled)
            statusBadge.text = statusText

            itemView.setOnClickListener { onCourseClick(course) }
        }
    }
}
