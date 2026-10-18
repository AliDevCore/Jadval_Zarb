package com.zarb.hero.ui.screens

import androidx.activity.compose.BackHandler
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
import com.zarb.hero.data.local.QuizEntity
import com.zarb.hero.model.TableMastery
import com.zarb.hero.ui.components.KidButton
import com.zarb.hero.util.PersianUtils

@Composable
fun HistoryScreen(
    quizzes: List<QuizEntity>,
    tableStats: List<TableMastery>,
    onBackHome: () -> Unit
) {
    BackHandler {
        onBackHome()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8E9))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBackHome,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("← بازگشت", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "تحلیل تسلط جدول‌های ضرب (۳ تا ۹)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )

                Spacer(modifier = Modifier.width(70.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(tableStats) { stat ->
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
                                text = "جدول ${PersianUtils.toPersian(stat.tableNumber)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (stat.totalAnswered < 3) {
                                Text(
                                    text = "داده ناکافی (کمتر از ۳ سؤال)",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            } else {
                                Text(
                                    text = "${PersianUtils.toPersian(stat.percentage)}٪ دقت",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (stat.percentage >= 80) Color(0xFF2E7D32) else Color(0xFFD84315)
                                )
                                Text(
                                    text = stat.statusText,
                                    fontSize = 13.sp,
                                    color = if (stat.percentage >= 80) Color(0xFF2E7D32) else Color(0xFFD84315)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "سوابق آزمون‌های اخیر:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(quizzes) { quiz ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "درصد: ${PersianUtils.toPersian(quiz.percentage)}٪",
                                fontWeight = FontWeight.Bold,
                                color = if (quiz.percentage >= 80) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                            Text(
                                text = "درست: ${PersianUtils.toPersian(quiz.correctCount)} از ۲۰",
                                fontSize = 14.sp
                            )
                            Text(
                                text = PersianUtils.formatDuration(quiz.durationSeconds),
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        KidButton(
            text = "بازگشت به خانه 🏠",
            onClick = onBackHome,
            containerColor = Color(0xFF00796B),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
