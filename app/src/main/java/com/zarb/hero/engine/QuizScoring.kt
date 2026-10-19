package com.zarb.hero.engine

import com.zarb.hero.model.ActiveQuizState
import com.zarb.hero.model.MistakeDetail
import com.zarb.hero.model.QuizSummary

/** منطق نمره‌دهی آزمون؛ مستقل از UI تا قابل تست باشد. */
object QuizScoring {
    const val QUIZ_SIZE = 20

    fun percentage(correctCount: Int, total: Int = QUIZ_SIZE): Int =
        if (total <= 0) 0 else (correctCount * 100) / total

    fun summarize(state: ActiveQuizState): QuizSummary {
        var correct = 0
        val mistakes = mutableListOf<MistakeDetail>()
        state.questions.forEachIndexed { index, q ->
            val answer = state.answers[index] ?: -1
            if (answer == q.correctAnswer) {
                correct++
            } else {
                mistakes.add(MistakeDetail(q.factorA, q.factorB, answer, q.correctAnswer))
            }
        }
        val total = state.questions.size
        return QuizSummary(
            quizId = state.quizId,
            totalQuestions = total,
            correctCount = correct,
            wrongCount = total - correct,
            percentage = percentage(correct, total),
            totalDurationSeconds = state.totalActiveSeconds,
            maxStreak = state.maxStreak,
            mistakes = mistakes
        )
    }
}
