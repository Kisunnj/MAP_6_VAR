package com.university.abiturient.model

data class Faculty(
    val id: Long,
    val name: String,
    val places: Int
)

data class Applicant(
    val id: Long,
    val facultyId: Long,
    val fullName: String,
    val certificateAvg: Double
)

data class Exam(
    val id: Long,
    val facultyId: Long,
    val code: String,
    val date: String,
    val subjectName: String
)

data class Grade(
    val id: Long,
    val examId: Long,
    val applicantId: Long,
    val value: Double
)

fun Applicant.combinedAverage(examAverage: Double?): Double {
    val exam = examAverage
    return if (exam == null || exam.isNaN()) {
        certificateAvg
    } else {
        (certificateAvg + exam) / 2.0
    }
}
