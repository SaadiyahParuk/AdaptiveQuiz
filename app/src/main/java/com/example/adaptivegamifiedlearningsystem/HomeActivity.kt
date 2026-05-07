package com.example.adaptivegamifiedlearningsystem

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : ComponentActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val db by lazy { QuizDbHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java)); finish(); return
        }
        setContent { MaterialTheme { HomeScreen() } }
    }

    @Preview
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun HomeScreen() {
        val uid = auth.currentUser!!.uid
        var sets by remember { mutableStateOf(db.getQuizSetsForUser(uid)) }
        var dialogState by remember { mutableStateOf<QuizSet?>(null) }
        var showDialog by remember { mutableStateOf(false) }
        var pendingDelete by remember { mutableStateOf<QuizSet?>(null) }

        fun reload() { sets = db.getQuizSetsForUser(uid) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("My Quizzes") },
                    actions = {
                        IconButton(onClick = {
                            auth.signOut()
                            startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                            finish()
                        }) { Icon(Icons.AutoMirrored.Filled.ExitToApp, "Sign out") }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { dialogState = null; showDialog = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("New Quiz") }
                )
            }
        ) { padding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sets, key = { it.id }) { set ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(set.title, style = MaterialTheme.typography.titleLarge)
                            if (set.description.isNotBlank())
                                Text(set.description, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalButton(
                                    onClick = {
                                        startActivity(
                                            Intent(this@HomeActivity, MainActivity::class.java)
                                                .putExtra("quizSetId", set.id)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PlayArrow, null); Text(" Take")
                                }
                                OutlinedButton(
                                    onClick = {
                                        // Open question editor
                                        startActivity(
                                            Intent(this@HomeActivity, QuizSetEditorActivity::class.java)
                                                .putExtra("quizSetId", set.id)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = set.ownerUserId == uid   // can't edit "example"
                                ) {
                                    Icon(Icons.Default.Edit, null); Text(" Questions")
                                }
                                IconButton(
                                    onClick = { pendingDelete = set },
                                    enabled = set.ownerUserId == uid
                                ) { Icon(Icons.Default.Delete, "Delete") }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            QuizSetDialog(
                initial = dialogState,
                onDismiss = { showDialog = false },
                onSave = { title, desc ->
                    if (dialogState == null) {
                        val s = QuizSet(0, uid, title, desc, System.currentTimeMillis())
                        val id = db.insertQuizSet(s)
                        // Firestore mirror
                        firestore.collection("users").document(uid)
                            .collection("quizSets").document(id.toString())
                            .set(mapOf("title" to title, "description" to desc,
                                "updatedAt" to s.updatedAt))
                    } else {
                        val s = dialogState!!.also {
                            it.title = title; it.description = desc
                        }
                        db.updateQuizSet(s)
                        firestore.collection("users").document(uid)
                            .collection("quizSets").document(s.id.toString())
                            .set(mapOf("title" to title, "description" to desc,
                                "updatedAt" to System.currentTimeMillis()))
                    }
                    showDialog = false; reload()
                }
            )
        }

        pendingDelete?.let { set ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                title = { Text("Delete \"${set.title}\"?") },
                text = { Text("This will remove all its questions.") },
                confirmButton = {
                    TextButton(onClick = {
                        db.deleteQuizSet(set.id)
                        firestore.collection("users").document(uid)
                            .collection("quizSets").document(set.id.toString())
                            .delete()
                        pendingDelete = null; reload()
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizSetDialog(
    initial: QuizSet?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New quiz set" else "Edit quiz set") },
        text = {
            Column {
                OutlinedTextField(title, { title = it }, label = { Text("Title") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(desc, { desc = it }, label = { Text("Description") })
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = { onSave(title, desc) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}