package com.zarb.hero.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestionEntity>)

    @Query("SELECT * FROM quizzes WHERE isCompleted = 1 ORDER BY timestamp DESC")
    fun getAllCompletedQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId ORDER BY questionIndex ASC")
    suspend fun getQuestionsForQuiz(quizId: Long): List<QuizQuestionEntity>

    @Query("SELECT * FROM quiz_questions WHERE isCorrect = 0 ORDER BY id DESC")
    fun getAllMistakes(): Flow<List<QuizQuestionEntity>>
}

@Dao
interface QuestionStatDao {
    @Query("SELECT * FROM question_stats")
    suspend fun getAllStats(): List<QuestionStatEntity>

    @Query("SELECT * FROM question_stats WHERE canonicalKey = :key LIMIT 1")
    suspend fun getStat(key: String): QuestionStatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStat(stat: QuestionStatEntity)
}

@Dao
interface AppPreferenceDao {
    @Query("SELECT * FROM app_preferences WHERE id = 1")
    fun getPreferences(): Flow<AppPreferenceEntity?>

    @Query("SELECT * FROM app_preferences WHERE id = 1")
    suspend fun getPreferencesSync(): AppPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(pref: AppPreferenceEntity)
}
