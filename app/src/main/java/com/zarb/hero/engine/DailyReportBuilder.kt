package com.zarb.hero.engine

import com.zarb.hero.data.local.QuizEntity
import com.zarb.hero.model.DailyUsageReport
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** تجمیع آزمون‌ها بر اساس تاریخ میلادی برای گزارش والدین. */
object DailyReportBuilder {
    fun build(
        quizzes: List<QuizEntity>,
        timeZone: TimeZone = TimeZone.getDefault()
    ): List<DailyUsageReport> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { this.timeZone = timeZone }
        return quizzes
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
}
