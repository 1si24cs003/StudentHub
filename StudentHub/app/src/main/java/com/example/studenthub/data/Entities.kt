package com.example.studenthub.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val syllabusNotes: String, // We'll store JSON here for modules/chapters
    val status: String = "Not Started" // e.g., "Not Started", "Ongoing", "Completed"
)

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subjectId: Int,
    val dateMillis: Long,
    val previousMarksScored: Float,
    val totalMarks: Float
)

@Entity(tableName = "notes")
data class PersonalNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val category: String
)
