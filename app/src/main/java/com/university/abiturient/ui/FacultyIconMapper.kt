package com.university.abiturient.ui

import androidx.annotation.DrawableRes
import com.university.abiturient.R

object FacultyIconMapper {
    @DrawableRes
    fun iconForFacultyId(facultyId: Long): Int = when (facultyId.toInt()) {
        1 -> R.drawable.ic_faculty_math
        2 -> R.drawable.ic_faculty_cs
        3 -> R.drawable.ic_faculty_physics
        else -> R.drawable.ic_faculty_default
    }
}
