package com.example.lb3.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.lb3.R

object StarRatingHelper {

    fun buildStars(
        context: Context,
        container: LinearLayout,
        rating: Float,
        interactive: Boolean = false,
        onRatingChanged: ((Int) -> Unit)? = null
    ) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(context)

        for (i in 1..5) {
            val star = inflater.inflate(R.layout.item_star, container, false) as ImageView
            val isFull = i <= rating
            star.setImageResource(if (isFull) R.drawable.ic_star_filled else R.drawable.ic_star_empty)

            if (interactive) {
                star.setOnClickListener { onRatingChanged?.invoke(i) }
            }
            container.addView(star)
        }
    }
}
