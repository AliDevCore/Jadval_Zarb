package com.zarb.hero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zarb.hero.model.QuizSummary
import com.zarb.hero.ui.components.KidButton
import com.zarb.hero.util.PersianUtils

@Composable
fun ReportScreen(
    summary: QuizSummary,
    bestPercentage: Int,
    onBackHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FBE7))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "کارنامه آزمون ۲۰ سؤالی",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF33691E)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "نمره شما: ${PersianUtils.toPersian(summary.percentage)}٪",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (summary.percentage >= 80) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "پاسخ‌های صحیح: ${PersianUtils.toPersian(summary.correctCount)} از ۲۰  |  غلط: ${PersianUtils.toPersian(summary.wrongCount)}",
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "مدت کل آزمون: ${PersianUtils.formatDuration(summary.totalDurationSeconds)}",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "بیشترین توالی درست در این آزمون: ${PersianUtils.toPersian(summary.maxStreak)} سؤال",
                        fontSize = 15.sp,
                        color = Color(0xFF1565C0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (summary.mistakes.isNotEmpty()) {
                Text(
                    text = "سؤال‌هایی که نیاز به یادآوری دارند:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(summary.mistakes) { mistake ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${PersianUtils.toPersian(mistake.factorA)} × ${PersianUtils.toPersian(mistake.factorB)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "پاسخ شما: ${PersianUtils.toPersian(mistake.childAnswer)}",
                                    color = Color.Red,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "درست: ${PersianUtils.toPersian(mistake.correctAnswer)}",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "آفرین قهرمان! بیست از بیست! همه پاسخ‌ها درست بود! 🏆",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }

        KidButton(
            text = "بازگشت به صفحه اصلی",
            onClick = onBackHome,
            containerColor = Color(0xFF00897B),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
