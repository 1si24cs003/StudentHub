package com.example.studenthub.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studenthub.data.AppDatabase
import com.example.studenthub.data.AppRepository
import com.example.studenthub.data.Exam
import com.example.studenthub.data.PersonalNote
import com.example.studenthub.data.Subject
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.defineFunction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

import android.content.Context
import android.content.SharedPreferences

class AiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    private val prefs: SharedPreferences = application.getSharedPreferences("student_hub_prefs", Context.MODE_PRIVATE)

    private val _userApiKey = MutableStateFlow(prefs.getString("gemini_api_key", "") ?: "")
    val userApiKey: StateFlow<String> = _userApiKey.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AppRepository(db.subjectDao(), db.examDao(), db.noteDao())
    }

    fun saveApiKey(newKey: String) {
        prefs.edit().putString("gemini_api_key", newKey.trim()).apply()
        _userApiKey.value = newKey.trim()
    }

    private val addSubjectFunction = defineFunction(
        name = "add_subject",
        description = "Add a new academic subject with its syllabus structure.",
        parameters = listOf(
            Schema.str("name", "The name of the subject, e.g., 'Mathematics'"),
            Schema.str("description", "A short description of the subject"),
            Schema.str("syllabusJson", "A JSON array string containing modules and chapters, e.g., '[{\"module\":\"Module 1\",\"chapters\":[\"Ch 1\", \"Ch 2\"]}]'")
        ),
        requiredParameters = listOf("name", "description", "syllabusJson")
    )
    
    private val addExamFunction = defineFunction(
        name = "add_exam",
        description = "Add an upcoming exam or assignment.",
        parameters = listOf(
            Schema.str("title", "The title of the exam, e.g., 'Midterm'"),
            Schema.int("subjectId", "The ID of the subject (use 1 if unknown)"),
            Schema.double("totalMarks", "The total marks possible")
        ),
        requiredParameters = listOf("title", "subjectId", "totalMarks")
    )

    private fun getGenerativeModel(key: String): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-3.6-flash",
            apiKey = key,
            tools = listOf(Tool(listOf(addSubjectFunction, addExamFunction)))
        )
    }

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(prompt: String, bitmap: Bitmap? = null) {
        if (prompt.isBlank() && bitmap == null) return
        
        val key = _userApiKey.value
        if (key.isBlank()) {
            _chatHistory.value = _chatHistory.value + ChatMessage(prompt.ifBlank { "Attached an image." }, isUser = true, image = bitmap)
            _chatHistory.value = _chatHistory.value + ChatMessage("Please enter your Gemini API Key in the settings (gear icon at the top right) to use AI features.", isUser = false)
            return
        }

        val userMessage = ChatMessage(prompt.ifBlank { "Attached an image." }, isUser = true, image = bitmap)
        _chatHistory.value = _chatHistory.value + userMessage
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val inputContent = content {
                    bitmap?.let { image(it) }
                    if (prompt.isNotBlank()) text(prompt)
                }
                
                val model = getGenerativeModel(key)
                val chat = model.startChat()
                val response = chat.sendMessage(inputContent)
                
                response.functionCalls.firstOrNull()?.let { functionCall ->
                    when (functionCall.name) {
                        "add_subject" -> {
                            val name = functionCall.args["name"] as? String ?: ""
                            val desc = functionCall.args["description"] as? String ?: ""
                            val syllabus = functionCall.args["syllabusJson"] as? String ?: "[]"
                            repository.insertSubject(Subject(name = name, description = desc, syllabusNotes = syllabus, status = "Not Started"))
                            _chatHistory.value = _chatHistory.value + ChatMessage("Subject '$name' added successfully via AI!", isUser = false)
                        }
                        "add_exam" -> {
                            val title = functionCall.args["title"] as? String ?: ""
                            val subjectId = (functionCall.args["subjectId"] as? Double)?.toInt() ?: 1
                            val totalMarks = (functionCall.args["totalMarks"] as? Double)?.toFloat() ?: 100f
                            repository.insertExam(Exam(title = title, subjectId = subjectId, dateMillis = System.currentTimeMillis() + 86400000, previousMarksScored = 0f, totalMarks = totalMarks))
                            _chatHistory.value = _chatHistory.value + ChatMessage("Exam '$title' added successfully via AI!", isUser = false)
                        }
                    }
                    val followUpResponse = chat.sendMessage(content {
                        part(
                            FunctionResponsePart(
                                functionCall.name,
                                JSONObject("{\"status\": \"success\"}")
                            )
                        )
                    })
                    val aiResponse = followUpResponse.text ?: "Task completed."
                    _chatHistory.value = _chatHistory.value + ChatMessage(aiResponse, isUser = false)
                } ?: run {
                    val aiResponse = response.text ?: "No response from AI."
                    _chatHistory.value = _chatHistory.value + ChatMessage(aiResponse, isUser = false)
                }
                
            } catch (e: Exception) {
                _chatHistory.value = _chatHistory.value + ChatMessage("Error: ${e.message}", isUser = false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean, val image: Bitmap? = null)
