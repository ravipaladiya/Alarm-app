package com.alarmapp.feature.challenges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun MathChallenge(
    difficulty: Int,
    onComplete: () -> Unit,
) {
    val problem = remember(difficulty) { generate(difficulty) }
    var input by remember { mutableStateOf("") }

    LaunchedEffect(input) {
        if (input.toIntOrNull() == problem.answer) onComplete()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = stringResource(R.string.challenge_math_prompt), style = MaterialTheme.typography.titleMedium)
        Text(text = problem.prompt, style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = input,
            onValueChange = { input = it.filter { ch -> ch.isDigit() || ch == '-' } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
    }
}

private data class Problem(val prompt: String, val answer: Int)

private fun generate(difficulty: Int): Problem {
    val range = when (difficulty.coerceIn(1, 3)) {
        1 -> 2..12
        2 -> 5..25
        else -> 10..40
    }
    val a = Random.nextInt(range.first, range.last)
    val b = Random.nextInt(range.first, range.last)
    val op = if (difficulty >= 2 && Random.nextBoolean()) "x" else "+"
    val answer = if (op == "+") a + b else a * b
    return Problem("$a $op $b = ?", answer)
}
