package com.zarb.hero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE0F7FA), Color(0xFFFFF9C4))
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = "قهرمان جدول ضرب",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF00695C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "یادگیری آسان، آزمون هیجان‌انگیز!",
                fontSize = 16.sp,
                color = Color(0xFF00796B)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "بهترین نتیجه", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            text = "${PersianUtils.toPersian(bestPercentage)}٪",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8F00)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "بیشترین زنجیره درست", fontSize = 14.sp, color = Color.Gray)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            KidButton(
                text = "شروع آزمون جدید (۲۰ سؤال)",
                onClick = onStartNewQuiz,
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
                .padding(bottom = 12.dp),
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
