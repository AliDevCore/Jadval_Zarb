package com.zarb.hero.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val quizId: Long,
    val timestamp: Long,
    val correctCount: Int,
    val wrongCount: Int,
    val percentage: Int,
    val durationSeconds: Long,
    val maxStreak: Int,
    val isCompleted: Boolean
)

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Long,
    val questionIndex: Int,
    val factorA: Int,
    val factorB: Int,
    val canonicalKey: String,
    val childAnswer: Int,
    val correctAnswer: Int,
    val isCorrect: Boolean,
    val timeSpentSeconds: Long
)

@Entity(tableName = "question_stats")
data class QuestionStatEntity(
    @PrimaryKey val canonicalKey: String,
    val factorA: Int,
    val factorB: Int,
    val totalAsked: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastWasWrong: Boolean = false,
    val lastWrongAnswer: Int? = null,
    val lastAskedTimestamp: Long = 0L
)

@Entity(tableName = "app_preferences")
data class AppPreferenceEntity(
    @PrimaryKey val id: Int = 1,
    val soundEnabled: Boolean = true,
    val bestPercentage: Int = 0,
    val maxStreakEver: Int = 0,
    val totalCompletedQuizzes: Int = 0,
    val incompleteQuizJson: String? = null
)
