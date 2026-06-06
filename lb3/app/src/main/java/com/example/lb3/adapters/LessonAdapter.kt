package com.example.lb3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lb3.R
import com.example.lb3.data.PreferencesManager
import com.example.lb3.models.Lesson

class LessonAdapter(
    private val prefs: PreferencesManager,
    private val userId: Int?,
    var showActions: Boolean = false,
    private val onToggle: (Lesson) -> Unit
) : RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    private var lessons: List<Lesson> = emptyList()

    fun submitList(list: List<Lesson>) {
        lessons = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(lessons[position])
    }

    override fun getItemCount(): Int = lessons.size

    inner class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.lesson_title)
        private val number: TextView = itemView.findViewById(R.id.lesson_number)
        private val action: TextView = itemView.findViewById(R.id.lesson_action)

        fun bind(lesson: Lesson) {
            title.text = lesson.title
            number.text = lesson.orderNum.toString()

            if (userId != null && showActions) {
                action.visibility = View.VISIBLE
                val completed = prefs.isLessonCompleted(userId, lesson.id)
                action.text = if (completed) {
                    itemView.context.getString(R.string.lesson_complete)
                } else {
                    itemView.context.getString(R.string.lesson_mark)
                }
                action.setOnClickListener { onToggle(lesson) }
            } else {
                action.visibility = View.GONE
                action.setOnClickListener(null)
            }
        }
    }
}
