package com.example.studenthub.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val subjectDao: SubjectDao,
    private val examDao: ExamDao,
    private val noteDao: NoteDao
) {
    fun getAllSubjects(): Flow<List<Subject>> = subjectDao.getAllSubjects()
    suspend fun insertSubject(subject: Subject) = subjectDao.insertSubject(subject)
    suspend fun deleteSubject(subject: Subject) = subjectDao.deleteSubject(subject)

    fun getAllExams(): Flow<List<Exam>> = examDao.getAllExams()
    suspend fun insertExam(exam: Exam) = examDao.insertExam(exam)
    suspend fun deleteExam(exam: Exam) = examDao.deleteExam(exam)

    fun getAllNotes(): Flow<List<PersonalNote>> = noteDao.getAllNotes()
    suspend fun insertNote(note: PersonalNote) = noteDao.insertNote(note)
    suspend fun deleteNote(note: PersonalNote) = noteDao.deleteNote(note)
}
