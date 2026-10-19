package com.zarb.hero.model

data class Question(
    val factorA: Int,
    val factorB: Int,
    val canonicalKey: String = if (factorA <= factorB) "${factorA}x${factorB}" else "${factorB}x${factorA}"
) {
    val correctAnswer: Int get() = factorA * factorB
}

data class ActiveQuizState(
    val quizId: Long = System.currentTimeMillis(),
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val answers: Map<Int, Int> = emptyMap(),
    val timePerQuestionSeconds: Map<Int, Long> = emptyMap(),
    val totalActiveSeconds: Long = 0L,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val isAnswerSubmitted: Boolean = false,
    val lastSubmittedAnswer: Int? = null,
    val isComplete: Boolean = false
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
}

data class QuizSummary(
    val quizId: Long,
    val totalQuestions: Int = 20,
    val correctCount: Int,
    val wrongCount: Int,
    val percentage: Int,
    val totalDurationSeconds: Long,
    val maxStreak: Int,
    val mistakes: List<MistakeDetail>
)

data class MistakeDetail(
    val factorA: Int,
    val factorB: Int,
    val childAnswer: Int,
    val correctAnswer: Int
)

data class TableMastery(
    val tableNumber: Int,
    val totalAnswered: Int,
    val correctCount: Int,
    val percentage: Int,
    val statusText: String
)

data class DailyUsageReport(
    val dateGregorian: String,
    val quizCount: Int,
    val correctCount: Int,
    val wrongCount: Int
)
