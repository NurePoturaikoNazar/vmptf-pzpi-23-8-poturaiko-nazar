package com.example.lb3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lb3.R
import com.example.lb3.models.Review
import com.example.lb3.utils.StarRatingHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private var reviews: List<Review> = emptyList()

    fun submitList(list: List<Review>) {
        reviews = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val author: TextView = itemView.findViewById(R.id.review_author)
        private val comment: TextView = itemView.findViewById(R.id.review_comment)
        private val stars: LinearLayout = itemView.findViewById(R.id.review_stars)
        private val date: TextView = itemView.findViewById(R.id.review_date)
        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        fun bind(review: Review) {
            author.text = review.userName
            comment.text = review.comment
            date.text = dateFormat.format(Date(review.createdAt))
            StarRatingHelper.buildStars(itemView.context, stars, review.rating.toFloat())
        }
    }
}
