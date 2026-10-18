package com.zarb.hero.ui.screens

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zarb.hero.engine.SoundManager
import com.zarb.hero.model.ActiveQuizState
import com.zarb.hero.ui.components.KidButton
import com.zarb.hero.ui.components.KidNumericKeypad
import com.zarb.hero.util.PersianUtils

@Composable
fun QuizScreen(
    state: ActiveQuizState,
    soundManager: SoundManager,
    onAnswerSubmitted: (Int, Long) -> Unit,
    onNextQuestion: () -> Unit,
    onExitQuiz: () -> Unit
) {
    var inputStr by remember(state.currentIndex) { mutableStateOf("") }
    var questionStartTime by remember(state.currentIndex) { mutableStateOf(SystemClock.elapsedRealtime()) }

    val currentQuestion = state.currentQuestion ?: return
    val isSubmitted = state.isAnswerSubmitted
    val isCorrect = state.lastSubmittedAnswer == currentQuestion.correctAnswer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onExitQuiz) {
                    Text("خروج", color = Color.Gray, fontSize = 16.sp)
                }
                Text(
                    text = "سؤال ${PersianUtils.toPersian(state.currentIndex + 1)} از ${PersianUtils.toPersian(state.questions.size)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF37474F)
                )
                Text(
                    text = "مانده: ${PersianUtils.toPersian(state.questions.size - state.currentIndex - 1)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            LinearProgressIndicator(
                progress = { (state.currentIndex + 1) / state.questions.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF00BCD4),
                trackColor = Color(0xFFE0E0E0)
            )
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${PersianUtils.toPersian(currentQuestion.factorA)}  ×  ${PersianUtils.toPersian(currentQuestion.factorB)}  =  ؟",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E88E5)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(60.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (inputStr.isEmpty()) "؟" else PersianUtils.toPersian(inputStr),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (inputStr.isEmpty()) Color.LightGray else Color.Black
                    )
                }
            }
        }

        AnimatedVisibility(visible = isSubmitted, enter = fadeIn()) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isCorrect) "آفرین! کاملاً درسته! 🌟" else "اشکالی نداره، پاسخ درست: ${PersianUtils.toPersian(currentQuestion.correctAnswer)} است.",
                        color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFD84315),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    KidButton(
                        text = if (state.currentIndex == 19) "مشاهده کارنامه پایانی" else "سؤال بعدی ⬅",
                        onClick = onNextQuestion,
                        containerColor = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
            }
        }

        if (!isSubmitted) {
            KidNumericKeypad(
                onDigitClick = { digit ->
                    if (inputStr.length < 3) inputStr += digit.toString()
                },
                onBackspace = {
                    if (inputStr.isNotEmpty()) inputStr = inputStr.dropLast(1)
                },
                onSubmit = {
                    val value = inputStr.toIntOrNull()
                    if (value != null) {
                        val duration = (SystemClock.elapsedRealtime() - questionStartTime) / 1000
                        if (value == currentQuestion.correctAnswer) {
                            soundManager.playCorrect()
                        } else {
                            soundManager.playWrong()
                        }
                        onAnswerSubmitted(value, duration)
                    }
                },
                canSubmit = inputStr.isNotEmpty()
            )
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
