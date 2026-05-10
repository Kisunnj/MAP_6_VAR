package com.university.abiturient.model

data class Note(
    val id: Long,
    val title: String,
    val body: String,
    val imagePath: String?,
    val createdAt: Long
)
