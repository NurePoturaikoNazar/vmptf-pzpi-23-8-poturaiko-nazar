package com.example.lb3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lb3.R
import com.example.lb3.data.MockDataRepository
import com.example.lb3.models.Teacher

class TeacherAdapter(private val compact: Boolean = false) : RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder>() {

    private var teachers: List<Teacher> = emptyList()

    fun submitList(list: List<Teacher>) {
        teachers = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherViewHolder {
        val layout = if (compact) R.layout.item_teacher_compact else R.layout.item_teacher
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return TeacherViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
        holder.bind(teachers[position])
    }

    override fun getItemCount(): Int = teachers.size

    inner class TeacherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.teacher_name)
        private val specialty: TextView = itemView.findViewById(R.id.teacher_specialty)
        private val bio: TextView? = itemView.findViewById(R.id.teacher_bio)
        private val coursesCount: TextView? = itemView.findViewById(R.id.course_count)
        private val avatar: TextView = itemView.findViewById(R.id.teacher_avatar)

        fun bind(teacher: Teacher) {
            name.text = teacher.name
            specialty.text = teacher.expertise
            bio?.text = teacher.bio
            avatar.text = teacher.name.take(1)

            val count = MockDataRepository.getCoursesForTeacher(teacher.id)
            coursesCount?.text = itemView.context.getString(R.string.courses_count, count)
        }
    }
}
