package com.example.lb3.models

data class Review(
    val id: Int,
    val userId: Int,
    val courseId: Int,
    val userName: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long
)
