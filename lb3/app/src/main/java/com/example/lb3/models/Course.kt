package com.example.lb3.models

data class Course(
    val id: Int,
    val title: String,
    val shortDescription: String,
    val fullDescription: String,
    val duration: String,
    val price: Int,
    val teacherId: Int,
    val headerColorIndex: Int
)
