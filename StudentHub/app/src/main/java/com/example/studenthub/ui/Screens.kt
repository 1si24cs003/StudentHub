package com.example.studenthub.ui

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.studenthub.data.Subject
import com.example.studenthub.notification.NotificationHelper
import com.example.studenthub.viewmodel.AiViewModel
import com.example.studenthub.viewmodel.StudentHubViewModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import androidx.compose.material.icons.filled.Delete
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: StudentHubViewModel) {
    val exams by viewModel.exams.collectAsState()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationHelper.createNotificationChannel(context)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NotificationHelper.createNotificationChannel(context)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Exam")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Upcoming Exams", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            if (exams.isEmpty()) {
                Text("No upcoming exams! You are free!")
            } else {
                LazyColumn {
                    items(exams) { exam ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(exam.title, style = MaterialTheme.typography.titleMedium)
                                    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(exam.dateMillis))
                                    Text("Date: $dateStr", style = MaterialTheme.typography.bodyMedium)
                                    Text("Total Marks: ${exam.totalMarks}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { viewModel.deleteExam(exam) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Exam", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showDialog) {
            var title by remember { mutableStateOf("") }
            var totalMarks by remember { mutableStateOf("100") }
            var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis() + 86400000L) }
            var showDatePicker by remember { mutableStateOf(false) }
            
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Upcoming Exam") },
                text = {
                    Column {
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Exam Title") })
                        OutlinedTextField(value = totalMarks, onValueChange = { totalMarks = it }, label = { Text("Total Marks") })
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
                        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Date: $dateStr")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val marks = totalMarks.toFloatOrNull() ?: 100f
                        val timeInMillis = selectedDateMillis
                        
                        viewModel.addExam(title, 1, timeInMillis, 0f, marks)
                        
                        val oneDayBefore = timeInMillis - 86400000L
                        val twoDaysBefore = timeInMillis - (2 * 86400000L)
                        val now = System.currentTimeMillis()
                        
                        if (twoDaysBefore > now) {
                            NotificationHelper.scheduleReminder(context, "Exam in 2 Days!", "Your $title exam is in 2 days. Keep studying!", twoDaysBefore)
                            NotificationHelper.scheduleReminder(context, "Exam Tomorrow!", "Your $title exam is tomorrow. Good luck!", oneDayBefore)
                        } else if (oneDayBefore > now) {
                            NotificationHelper.scheduleReminder(context, "Exam Tomorrow!", "Your $title exam is tomorrow. Good luck!", oneDayBefore)
                        } else if (timeInMillis > now) {
                            NotificationHelper.scheduleReminder(context, "Upcoming Exam!", "Your $title exam is coming up soon. Start studying!", now + 10000L)
                        }
                        
                        showDialog = false
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicsScreen(viewModel: StudentHubViewModel) {
    val subjects by viewModel.subjects.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Academics Board") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Subject")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            if (subjects.isEmpty()) {
                item { Text("No subjects added yet. Add one or ask AI!") }
            } else {
                items(subjects) { subject ->
                    MondayBoard(subject, viewModel)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (showDialog) {
            var name by remember { mutableStateOf("") }
            var desc by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Subject") },
                text = {
                    Column {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Subject Name") })
                        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.addSubject(name, desc, "[]", "Not Started")
                        showDialog = false
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MondayBoard(subject: Subject, viewModel: StudentHubViewModel) {
    val modules = remember(subject.syllabusNotes) { parseModules(subject.syllabusNotes) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header (Subject Name)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ExpandMore, contentDescription = null, tint = Color(0xFF579BFC))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = subject.name,
                color = Color(0xFF579BFC),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.deleteSubject(subject) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Subject", tint = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        // Table
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                Text("Item", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text("Status", modifier = Modifier.width(100.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outlineVariant))
                Text("START", modifier = Modifier.width(90.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
            }
            HorizontalDivider()
            
            // Rows
            modules.forEachIndexed { index, module ->
                var subItemsExpanded by remember { mutableStateOf(false) }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left colored edge
                    Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(Color(0xFF579BFC)))
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    IconButton(onClick = { subItemsExpanded = !subItemsExpanded }, modifier = Modifier.size(24.dp)) {
                        Icon(if (subItemsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(module.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    
                    // Status Box
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight()
                            .background(getStatusColor(module.status))
                            .clickable { menuExpanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = module.status.ifEmpty { " " },
                            color = if (module.status.isEmpty()) MaterialTheme.colorScheme.onSurface else Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            val statuses = listOf("Working on it", "Stuck", "Done", "")
                            statuses.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st.ifEmpty { "Clear" }) },
                                    onClick = {
                                        menuExpanded = false
                                        val newJson = updateModuleStatus(subject.syllabusNotes, index, st)
                                        viewModel.updateSubject(subject.copy(syllabusNotes = newJson))
                                    }
                                )
                            }
                        }
                    }
                    
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outlineVariant))
                    
                    // Date Box
                    var showDatePicker by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .fillMaxHeight()
                            .clickable { showDatePicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.padding(4.dp).fillMaxWidth().fillMaxHeight()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = module.date,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                    
                    if (showDatePicker) {
                        val datePickerState = rememberDatePickerState()
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    val time = datePickerState.selectedDateMillis
                                    if (time != null) {
                                        val date = SimpleDateFormat("MMM dd", Locale.getDefault()).format(
                                            Date(time)
                                        )
                                        val newJson = updateModuleDate(subject.syllabusNotes, index, date)
                                        viewModel.updateSubject(subject.copy(syllabusNotes = newJson))
                                    }
                                    showDatePicker = false
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }
                }
                HorizontalDivider()
                
                if (subItemsExpanded) {
                    module.chapters.forEach { chapter ->
                        Row(
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(44.dp))
                            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.LightGray))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(chapter, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        }
                        HorizontalDivider()
                    }
                    
                    var isAddingChapter by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth().height(36.dp).clickable { isAddingChapter = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(44.dp))
                        Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.LightGray))
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        if (isAddingChapter) {
                            var newChapName by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = newChapName,
                                onValueChange = { newChapName = it },
                                modifier = Modifier.weight(1f).padding(vertical = 2.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            IconButton(onClick = {
                                if (newChapName.isNotBlank()) {
                                    val newJson = addChapter(subject.syllabusNotes, index, newChapName)
                                    viewModel.updateSubject(subject.copy(syllabusNotes = newJson))
                                }
                                isAddingChapter = false
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Text("+ Add subitem", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.width(190.dp))
                    }
                    HorizontalDivider()
                }
            }
            
            // + Add Item Row
            var isAdding by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { isAdding = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                if (isAdding) {
                    var newItemName by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    IconButton(onClick = {
                        if (newItemName.isNotBlank()) {
                            val newJson = addModule(subject.syllabusNotes, newItemName)
                            viewModel.updateSubject(subject.copy(syllabusNotes = newJson))
                        }
                        isAdding = false
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                } else {
                    Text("+ Add item", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.width(190.dp))
            }
        }
    }
}

data class ModuleItem(val name: String, val status: String, val date: String, val chapters: List<String>)

fun parseModules(json: String): List<ModuleItem> {
    val list = mutableListOf<ModuleItem>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val chapters = mutableListOf<String>()
            val chaptersArray = obj.optJSONArray("chapters")
            if (chaptersArray != null) {
                for (j in 0 until chaptersArray.length()) {
                    chapters.add(chaptersArray.getString(j))
                }
            }
            list.add(
                ModuleItem(
                    name = obj.optString("module", "Unnamed"),
                    status = obj.optString("status", ""),
                    date = obj.optString("date", "-"),
                    chapters = chapters
                )
            )
        }
    } catch (e: Exception) {
        // Return empty on error
    }
    return list
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Working on it" -> Color(0xFFFDAB3D)
        "Stuck" -> Color(0xFFE2445C)
        "Done" -> Color(0xFF00C875)
        else -> Color(0xFFC4C4C4)
    }
}

fun updateModuleStatus(json: String, index: Int, newStatus: String): String {
    return try {
        val array = JSONArray(json)
        val obj = array.getJSONObject(index)
        obj.put("status", newStatus)
        array.put(index, obj)
        array.toString()
    } catch (e: Exception) {
        json
    }
}

fun addModule(json: String, name: String): String {
    return try {
        val array = if (json.isNotBlank() && json.trim().startsWith("[")) JSONArray(json) else JSONArray()
        val obj = JSONObject()
        obj.put("module", name)
        obj.put("status", "")
        obj.put("date", "-")
        obj.put("chapters", JSONArray())
        array.put(obj)
        array.toString()
    } catch (e: Exception) {
        "[]"
    }
}

fun updateModuleDate(json: String, index: Int, newDate: String): String {
    return try {
        val array = JSONArray(json)
        val obj = array.getJSONObject(index)
        obj.put("date", newDate)
        array.put(index, obj)
        array.toString()
    } catch (e: Exception) {
        json
    }
}

fun addChapter(json: String, moduleIndex: Int, chapterName: String): String {
    return try {
        val array = JSONArray(json)
        val moduleObj = array.getJSONObject(moduleIndex)
        val chaptersArray = moduleObj.optJSONArray("chapters") ?: JSONArray()
        chaptersArray.put(chapterName)
        moduleObj.put("chapters", chaptersArray)
        array.put(moduleIndex, moduleObj)
        array.toString()
    } catch (e: Exception) {
        json
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalScreen(viewModel: StudentHubViewModel) {
    val notes by viewModel.notes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Personal Notes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (notes.isEmpty()) {
                Text("No notes found.")
            } else {
                LazyColumn {
                    items(notes) { note ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(note.title, style = MaterialTheme.typography.titleMedium)
                                    Text(note.body, style = MaterialTheme.typography.bodyMedium)
                                    Text("Category: ${note.category}", style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(onClick = { viewModel.deleteNote(note) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Note", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            var title by remember { mutableStateOf("") }
            var body by remember { mutableStateOf("") }
            var addReminder by remember { mutableStateOf(false) }
            
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Note") },
                text = {
                    Column {
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                        OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Content") })
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            Checkbox(checked = addReminder, onCheckedChange = { addReminder = it })
                            Text("Remind me about this (in 15s for demo)")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.addNote(title, body, "General")
                        if (addReminder) {
                            val timeInMillis = System.currentTimeMillis() + 15000L // 15 seconds for testing
                            NotificationHelper.scheduleReminder(context, "Note Reminder: $title", body, timeInMillis)
                        }
                        showDialog = false
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(aiViewModel: AiViewModel) {
    val chatHistory by aiViewModel.chatHistory.collectAsState()
    val isLoading by aiViewModel.isLoading.collectAsState()
    var prompt by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("AI Assistant") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(chatHistory) { message ->
                    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
                    val color = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = alignment) {
                        Card(colors = CardDefaults.cardColors(containerColor = color)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                message.image?.let {
                                    Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.height(150.dp).padding(bottom = 8.dp))
                                }
                                Text(text = message.text)
                            }
                        }
                    }
                }
                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            if (selectedImageUri != null) {
                Text(
                    text = "Image attached. Tap to remove.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp).clickable { selectedImageUri = null }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Default.Image, contentDescription = "Add Image")
                }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask Gemini...") },
                    enabled = !isLoading,
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    var bitmap: Bitmap? = null
                    selectedImageUri?.let { uri ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(context.contentResolver, uri)
                            bitmap = ImageDecoder.decodeBitmap(source)
                        } else {
                            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        }
                    }
                    aiViewModel.sendMessage(prompt, bitmap)
                    prompt = ""
                    selectedImageUri = null
                }, enabled = (prompt.isNotBlank() || selectedImageUri != null) && !isLoading) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}