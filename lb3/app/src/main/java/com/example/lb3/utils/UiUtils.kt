package com.example.lb3.utils

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.lb3.R
import com.example.lb3.models.CourseStatus

object UiUtils {

    fun applyStatusBadge(context: Context, view: TextView, status: CourseStatus) {
        when (status) {
            CourseStatus.AVAILABLE -> {
                view.text = context.getString(R.string.status_available)
                view.setBackgroundResource(R.drawable.bg_badge_available)
                view.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }
            CourseStatus.IN_PROGRESS -> {
                view.text = context.getString(R.string.status_in_progress)
                view.setBackgroundResource(R.drawable.bg_badge_progress)
                view.setTextColor(ContextCompat.getColor(context, R.color.primary))
            }
            CourseStatus.COMPLETED -> {
                view.text = context.getString(R.string.status_completed)
                view.setBackgroundResource(R.drawable.bg_badge_completed)
                view.setTextColor(ContextCompat.getColor(context, R.color.success_text))
            }
        }
    }

    fun headerColor(context: Context, index: Int): Int {
        val colors = listOf(
            R.color.course_header_1,
            R.color.course_header_2,
            R.color.course_header_3,
            R.color.course_header_4,
            R.color.course_header_5,
            R.color.course_header_6
        )
        val colorRes = colors[index % colors.size]
        return ContextCompat.getColor(context, colorRes)
    }
}
