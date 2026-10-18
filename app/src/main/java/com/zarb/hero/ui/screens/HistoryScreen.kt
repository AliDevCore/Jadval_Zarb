package com.zarb.hero.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zarb.hero.data.local.QuizEntity
import com.zarb.hero.model.DailyUsageReport
import com.zarb.hero.model.TableMastery
import com.zarb.hero.ui.components.KidButton
import com.zarb.hero.util.PersianUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    quizzes: List<QuizEntity>,
    tableStats: List<TableMastery>,
    onBackHome: () -> Unit
) {
    // بازگشت با دکمه فیزیکی یا ژست گوشی به خانه
    BackHandler {
        onBackHome()
    }

    // تب انتخابی: ۰ = گزارش روزانه (کنترل والدین)، ۱ = تحلیل تسلط جدول‌ها
    var selectedTab by remember { mutableIntStateOf(0) }

    // تجمیع آزمون‌ها بر اساس تاریخ میلادی
    val dailyReports = remember(quizzes) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        quizzes
            .groupBy { sdf.format(Date(it.timestamp)) }
            .map { (dateStr, list) ->
                DailyUsageReport(
                    dateGregorian = dateStr,
                    quizCount = list.size,
                    correctCount = list.sumOf { it.correctCount },
                    wrongCount = list.sumOf { it.wrongCount }
                )
            }
            .sortedByDescending { it.dateGregorian }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8E9))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // عنوان متمرکز بدون دکمه بالای صفحه
            Text(
                text = "کارنامه و گزارش سوابق",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1B5E20),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // سوئیچ دو حالته شیک و تمیز برای انتخاب نوع گزارش
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFDCEDC8))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 0) Color(0xFF00796B) else Color.Transparent)
                        .clickable { selectedTab = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📅 گزارش روزانه آزمون‌ها",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) Color.White else Color(0xFF2E7D32)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 1) Color(0xFF00796B) else Color.Transparent)
                        .clickable { selectedTab = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📊 تحلیل جدول‌ها (۳ تا ۹)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) Color.White else Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // تب ۱: جدول گزارش روزانه اختصاصی
                if (dailyReports.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هنوز آزمونی ثبت نشده است.",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // هدر جدول با ۴ ستون درخواستی
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE0F2F1))
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تاریخ (میلادی)",
                                    modifier = Modifier.weight(1.35f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF004D40),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "تعداد آزمون",
                                    modifier = Modifier.weight(0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF004D40),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "پاسخ صحیح",
                                    modifier = Modifier.weight(0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "پاسخ اشتباه",
                                    modifier = Modifier.weight(0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Divider(color = Color(0xFFB2DFDB), thickness = 1.dp)

                            // سطرهای جدول
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(dailyReports) { report ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = report.dateGregorian,
                                            modifier = Modifier.weight(1.35f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF37474F),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "${PersianUtils.toPersian(report.quizCount)} آزمون",
                                            modifier = Modifier.weight(0.9f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00796B),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = PersianUtils.toPersian(report.correctCount),
                                            modifier = Modifier.weight(0.9f),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = PersianUtils.toPersian(report.wrongCount),
                                            modifier = Modifier.weight(0.9f),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD32F2F),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            } else {
                // تب ۲: تحلیل تسلط جدول‌ها و سوابق اخیر
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "میزان تسلط در هر جدول ضرب:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

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

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "سوابق آزمون‌های اخیر:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

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
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // تنها دکمه بازگشت در پایین صفحه
        KidButton(
            text = "بازگشت به خانه 🏠",
            onClick = onBackHome,
            containerColor = Color(0xFF00796B),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
