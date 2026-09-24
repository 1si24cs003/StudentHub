package com.example.studenthub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studenthub.data.AppDatabase
import com.example.studenthub.data.AppRepository
import com.example.studenthub.data.Exam
import com.example.studenthub.data.PersonalNote
import com.example.studenthub.data.Subject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudentHubViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AppRepository(db.subjectDao(), db.examDao(), db.noteDao())
    }

    val subjects = repository.getAllSubjects().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val exams = repository.getAllExams().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notes = repository.getAllNotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addSubject(name: String, desc: String, syllabus: String, status: String = "Not Started") {
        viewModelScope.launch {
            repository.insertSubject(Subject(name = name, description = desc, syllabusNotes = syllabus, status = status))
        }
    }
    
    fun updateSubjectStatus(subject: Subject, newStatus: String) {
        viewModelScope.launch {
            repository.insertSubject(subject.copy(status = newStatus))
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.insertSubject(subject)
        }
    }

    fun addExam(title: String, subjectId: Int, dateMillis: Long, prevMarks: Float, totalMarks: Float) {
        viewModelScope.launch {
            repository.insertExam(Exam(
                title = title,
                subjectId = subjectId,
                dateMillis = dateMillis,
                previousMarksScored = prevMarks,
                totalMarks = totalMarks
            ))
        }
    }

    fun addNote(title: String, body: String, category: String) {
        viewModelScope.launch {
            repository.insertNote(PersonalNote(title = title, body = body, category = category))
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    fun deleteExam(exam: Exam) {
        viewModelScope.launch {
            repository.deleteExam(exam)
        }
    }

    fun deleteNote(note: PersonalNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
}
