package com.zarb.hero.engine

import com.zarb.hero.data.local.QuizQuestionEntity
import com.zarb.hero.model.TableMastery

/** محاسبه میزان تسلط در هر جدول ضرب (۳ تا ۹). سؤال a×b در آمار هر دو جدول a و b شمرده می‌شود. */
object TableMasteryCalculator {
    fun calculate(questions: List<QuizQuestionEntity>): List<TableMastery> =
        (3..9).map { num ->
            val relevant = questions.filter { it.factorA == num || it.factorB == num }
            val total = relevant.size
            val correct = relevant.count { it.isCorrect }
            val pct = if (total > 0) (correct * 100) / total else 0
            val status = when {
                total < 3 -> "داده ناکافی"
                pct >= 85 -> "تسلط عالی 🏆"
                pct >= 70 -> "خوب 👍"
                else -> "نیاز به تمرین ✏️"
            }
            TableMastery(num, total, correct, pct, status)
        }
}
