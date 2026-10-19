package com.zarb.hero.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zarb.hero.R
import com.zarb.hero.ui.components.KidButton
import com.zarb.hero.util.PersianUtils

@Composable
fun HomeScreen(
    bestPercentage: Int,
    maxStreak: Int,
    hasIncompleteQuiz: Boolean,
    isSoundOn: Boolean,
    onStartNewQuiz: () -> Unit,
    onResumeQuiz: () -> Unit,
    onViewHistory: () -> Unit,
    onReviewMistakes: () -> Unit,
    onToggleSound: () -> Unit
) {
    var showDiscardDialog by remember { mutableStateOf(false) }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("شروع آزمون جدید؟") },
            text = { Text("آزمون نیمه‌تمام قبلی از بین می‌رود. مطمئنی؟") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onStartNewQuiz()
                }) { Text("بله، آزمون جدید") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("انصراف") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE0F7FA), Color(0xFFFFF9C4))
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            // لوگوی جذاب و کودک‌پسند در بالای صفحه
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "لوگوی قهرمان جدول ضرب",
                modifier = Modifier
                    .size(96.dp)
                    .shadow(8.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "قهرمان جدول ضرب",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF00695C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "تمرین هوشمند اعداد ۳ تا ۹",
                fontSize = 15.sp,
                color = Color(0xFF00796B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "بهترین نتیجه", fontSize = 13.sp, color = Color.Gray)
                        Text(
                            text = "${PersianUtils.toPersian(bestPercentage)}٪",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8F00)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "بیشترین زنجیره درست", fontSize = 13.sp, color = Color.Gray)
                        Text(
                            text = PersianUtils.toPersian(maxStreak),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KidButton(
                text = "شروع آزمون جدید (۲۰ سؤال)",
                onClick = {
                    if (hasIncompleteQuiz) showDiscardDialog = true else onStartNewQuiz()
                },
                containerColor = Color(0xFF00B0FF),
                modifier = Modifier.fillMaxWidth()
            )

            if (hasIncompleteQuiz) {
                KidButton(
                    text = "ادامه آزمون قبلی",
                    onClick = onResumeQuiz,
                    containerColor = Color(0xFF7C4DFF),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            KidButton(
                text = "مرور اشتباهات گذشته",
                onClick = onReviewMistakes,
                containerColor = Color(0xFFFF7043),
                modifier = Modifier.fillMaxWidth()
            )

            KidButton(
                text = "کارنامه و سوابق جدول‌ها",
                onClick = onViewHistory,
                containerColor = Color(0xFF26A69A),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = onToggleSound,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isSoundOn) "صدا: روشن 🔊" else "صدا: خاموش 🔇",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
