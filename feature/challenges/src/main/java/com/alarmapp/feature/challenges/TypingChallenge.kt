package com.alarmapp.feature.challenges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

@Composable
fun TypingChallenge(
    text: String,
    onComplete: () -> Unit,
) {
    var input by remember { mutableStateOf("") }
    LaunchedEffect(input) {
        if (input.trim().equals(text.trim(), ignoreCase = true)) onComplete()
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Type this exactly:", style = MaterialTheme.typography.titleMedium)
        Text(text, style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(value = input, onValueChange = { input = it }, singleLine = true)
    }
}
