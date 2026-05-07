package com.example.adaptivegamifiedlearningsystem

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.adaptivegamifiedlearningsystem.network.Network
import com.example.adaptivegamifiedlearningsystem.network.PredictRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : ComponentActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Already signed in? Skip straight to home.
        if (auth.currentUser != null) {
            goHome(); return
        }
        setContent { MaterialTheme { LoginScreen() } }
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun LoginScreen() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        var loading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        // Warm-up the Render API in the background while the user types
        LaunchedEffect(Unit) {
            scope.launch(Dispatchers.IO) {
                runCatching { Network.api.predict(PredictRequest(listOf(2,0,0,10,10))) }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("LearnQuest", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(16.dp))

            Button(
                enabled = !loading && email.isNotBlank() && password.length >= 6,
                onClick = {
                    loading = true; error = null
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener { goHome() }
                        .addOnFailureListener { e ->
                            loading = false; error = e.localizedMessage
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (loading) "Signing in…" else "Sign in") }

            TextButton(
                enabled = !loading && email.isNotBlank() && password.length >= 6,
                onClick = {
                    loading = true; error = null
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { goHome() }
                        .addOnFailureListener { e ->
                            loading = false; error = e.localizedMessage
                        }
                }
            ) { Text("Create account") }
        }
    }
}