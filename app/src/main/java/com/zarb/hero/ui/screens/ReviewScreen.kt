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
import com.zarb.hero.data.local.QuestionStatEntity
import com.zarb.hero.ui.components.KidButton
import com.zarb.hero.util.PersianUtils

@Composable
fun ReviewMistakesScreen(
    mistakeStats: List<QuestionStatEntity>,
    onBackHome: () -> Unit
) {
    BackHandler {
        onBackHome()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3E0))
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("← بازگشت", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "جعبه مرور اشتباهات",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFE65100)
                )

                Spacer(modifier = Modifier.width(70.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "سؤال‌هایی که قبلاً به آن‌ها اشتباه پاسخ داده‌اید:",
                fontSize = 14.sp,
                color = Color(0xFF5D4037)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (mistakeStats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ سؤال اشتباهی ثبت نشده است! شما فوق‌العاده‌اید! 🎉",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mistakeStats) { stat ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${PersianUtils.toPersian(stat.factorA)} × ${PersianUtils.toPersian(stat.factorB)} = ${PersianUtils.toPersian(stat.factorA * stat.factorB)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00796B)
                                    )
                                    Text(
                                        text = "${PersianUtils.toPersian(stat.wrongCount)} بار خطا",
                                        color = Color.Red,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "پاسخ‌های درست: ${PersianUtils.toPersian(stat.correctCount)} از ${PersianUtils.toPersian(stat.totalAsked)}",
                                        fontSize = 13.sp,
                                        color = Color.DarkGray
                                    )
                                    if (stat.lastWrongAnswer != null) {
                                        Text(
                                            text = "آخرین جواب غلط: ${PersianUtils.toPersian(stat.lastWrongAnswer)}",
                                            fontSize = 13.sp,
                                            color = Color(0xFFC2185B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        KidButton(
            text = "بازگشت به خانه 🏠",
            onClick = onBackHome,
            containerColor = Color(0xFFFF6F00),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
