package com.example.adaptivegamifiedlearningsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

class QuizSetEditorActivity : ComponentActivity() {

    private lateinit var db: QuizDbHelper
    private var quizSetId: Long = -1L
    private var quizSetTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = QuizDbHelper(this)
        quizSetId = intent.getLongExtra("quizSetId", -1L)
        quizSetTitle = intent.getStringExtra("quizSetTitle") ?: "Edit Quiz"

        setContent {
            MaterialTheme {
                EditorScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EditorScreen() {
        var questions by remember { mutableStateOf(db.getQuestionsForSet(quizSetId)) }
        var showDialog by remember { mutableStateOf(false) }
        var editing: Questions? by remember { mutableStateOf(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(quizSetTitle) },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    editing = null
                    showDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add question")
                }
            }
        ) { padding ->
            if (questions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No questions yet. Tap + to add one.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(questions, key = { it.id }) { q ->
                        QuestionCard(
                            q = q,
                            onEdit = { editing = q; showDialog = true },
                            onDelete = {
                                db.deleteQuestion(q.id)
                                mirrorDelete(q.id)
                                questions = db.getQuestionsForSet(quizSetId)
                            }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            QuestionDialog(
                existing = editing,
                onDismiss = { showDialog = false },
                onSave = { text, o1, o2, o3, o4, ans, diff ->
                    val saved: Questions
                    if (editing == null) {
                        // Build a Questions object first
                        val newQ = Questions().apply {
                            question = text
                            option1 = o1; option2 = o2; option3 = o3; option4 = o4
                            answerNr = ans
                            difficulty = diff
                        }
                        val newId = db.insertQuestion(quizSetId, newQ, diff)
                        newQ.id = newId
                        saved = newQ
                    } else {
                        saved = editing!!.apply {
                            question = text
                            option1 = o1; option2 = o2; option3 = o3; option4 = o4
                            answerNr = ans
                            difficulty = diff
                        }
                        db.updateQuestion(saved)
                    }
                    mirrorSave(saved)
                    questions = db.getQuestionsForSet(quizSetId)
                    showDialog = false
                }
            )
        }
    }

    @Composable
    fun QuestionCard(q: Questions, onEdit: () -> Unit, onDelete: () -> Unit) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        q.question,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(difficultyLabel(q.difficulty)) }
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text("A) ${q.option1}")
                Text("B) ${q.option2}")
                Text("C) ${q.option3}")
                Text("D) ${q.option4}")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Correct: ${listOf("A","B","C","D")[q.answerNr - 1]}",
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(4.dp)); Text("Edit")
                    }
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(4.dp)); Text("Delete")
                    }
                }
            }
        }
    }

    @Composable
    fun QuestionDialog(
        existing: Questions?,
        onDismiss: () -> Unit,
        onSave: (String, String, String, String, String, Int, Int) -> Unit
    ) {
        var text by remember { mutableStateOf(existing?.question ?: "") }
        var o1 by remember { mutableStateOf(existing?.option1 ?: "") }
        var o2 by remember { mutableStateOf(existing?.option2 ?: "") }
        var o3 by remember { mutableStateOf(existing?.option3 ?: "") }
        var o4 by remember { mutableStateOf(existing?.option4 ?: "") }
        var ans by remember { mutableStateOf(existing?.answerNr ?: 1) }
        var diff by remember { mutableStateOf(existing?.difficulty ?: 2) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(if (existing == null) "Add Question" else "Edit Question") },
            text = {
                Column {
                    OutlinedTextField(value = text, onValueChange = { text = it },
                        label = { Text("Question") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(value = o1, onValueChange = { o1 = it },
                        label = { Text("Option A") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = o2, onValueChange = { o2 = it },
                        label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = o3, onValueChange = { o3 = it },
                        label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = o4, onValueChange = { o4 = it },
                        label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("Correct Answer:")
                    Row {
                        listOf("A","B","C","D").forEachIndexed { i, label ->
                            FilterChip(
                                selected = ans == i + 1,
                                onClick = { ans = i + 1 },
                                label = { Text(label) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Difficulty:")
                    Row {
                        listOf(1 to "Easy", 2 to "Medium", 3 to "Hard").forEach { (v, lbl) ->
                            FilterChip(
                                selected = diff == v,
                                onClick = { diff = v },
                                label = { Text(lbl) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = text.isNotBlank() &&
                            listOf(o1, o2, o3, o4).all { it.isNotBlank() },
                    onClick = { onSave(text, o1, o2, o3, o4, ans, diff) }
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
        )
    }

    private fun difficultyLabel(d: Int) = when (d) {
        1 -> "Easy"; 3 -> "Hard"; else -> "Medium"
    }

    private fun mirrorSave(q: Questions) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val data = mapOf(
            "question" to q.question,
            "option1" to q.option1, "option2" to q.option2,
            "option3" to q.option3, "option4" to q.option4,
            "answerNr" to q.answerNr,
            "difficulty" to q.difficulty
        )
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .collection("quizSets").document(quizSetId.toString())
            .collection("questions").document(q.id.toString())
            .set(data)
    }

    private fun mirrorDelete(qid: Long) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .collection("quizSets").document(quizSetId.toString())
            .collection("questions").document(qid.toString())
            .delete()
    }
}