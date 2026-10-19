package com.zarb.hero.data

import androidx.room.withTransaction
import com.zarb.hero.data.local.AppDatabase
import com.zarb.hero.data.local.AppPreferenceEntity
import com.zarb.hero.data.local.QuestionStatEntity
import com.zarb.hero.data.local.QuizEntity
import com.zarb.hero.data.local.QuizQuestionEntity
import com.zarb.hero.engine.ActiveQuizCodec
import com.zarb.hero.model.ActiveQuizState
import com.zarb.hero.model.QuizSummary
import kotlinx.coroutines.flow.Flow
import kotlin.math.max

class QuizRepository(private val db: AppDatabase) {

    val completedQuizzes: Flow<List<QuizEntity>> = db.quizDao().getAllCompletedQuizzes()
    val allQuestions: Flow<List<QuizQuestionEntity>> = db.quizDao().observeAllQuestions()

    suspend fun loadPreferences(): AppPreferenceEntity {
        val dao = db.appPreferenceDao()
        dao.insertIfAbsent(AppPreferenceEntity())
        return dao.getPreferencesSync() ?: AppPreferenceEntity()
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        val dao = db.appPreferenceDao()
        dao.insertIfAbsent(AppPreferenceEntity())
        dao.updateSound(enabled)
    }

    suspend fun loadStats(): Map<String, QuestionStatEntity> =
        db.questionStatDao().getAllStats().associateBy { it.canonicalKey }

    suspend fun loadMistakeStats(): List<QuestionStatEntity> =
        db.questionStatDao().getAllStats()
            .filter { it.wrongCount > 0 }
            .sortedByDescending { it.wrongCount }

    /** آزمون نیمه‌تمام ذخیره‌شده؛ در صورت نبود یا خرابی داده، null. */
    suspend fun loadActiveQuiz(): ActiveQuizState? =
        loadPreferences().incompleteQuizJson?.let { ActiveQuizCodec.decode(it) }

    /** ذخیره (یا با null پاک‌کردن) وضعیت آزمون در حال انجام. */
    suspend fun saveActiveQuiz(state: ActiveQuizState?) {
        val dao = db.appPreferenceDao()
        dao.insertIfAbsent(AppPreferenceEntity())
        dao.updateIncompleteQuiz(state?.let { ActiveQuizCodec.encode(it) })
    }

    /**
     * ثبت نتیجه نهایی آزمون در یک تراکنش واحد: یا همه چیز ذخیره می‌شود یا هیچ چیز.
     * ثبت تکراری یک quizId نادیده گرفته می‌شود.
     */
    suspend fun saveCompletedQuiz(state: ActiveQuizState, summary: QuizSummary): AppPreferenceEntity =
        db.withTransaction {
            val quizDao = db.quizDao()
            val statDao = db.questionStatDao()
            val prefDao = db.appPreferenceDao()

            prefDao.insertIfAbsent(AppPreferenceEntity())
            val pref = prefDao.getPreferencesSync() ?: AppPreferenceEntity()

            if (quizDao.countQuiz(summary.quizId) > 0) {
                return@withTransaction pref
            }

            val now = System.currentTimeMillis()
            quizDao.insertQuiz(
                QuizEntity(
                    quizId = summary.quizId,
                    timestamp = now,
                    correctCount = summary.correctCount,
                    wrongCount = summary.wrongCount,
                    percentage = summary.percentage,
                    durationSeconds = summary.totalDurationSeconds,
                    maxStreak = summary.maxStreak,
                    isCompleted = true
                )
            )

            val questionEntities = state.questions.mapIndexed { idx, q ->
                val ans = state.answers[idx] ?: -1
                QuizQuestionEntity(
                    quizId = summary.quizId,
                    questionIndex = idx + 1,
                    factorA = q.factorA,
                    factorB = q.factorB,
                    canonicalKey = q.canonicalKey,
                    childAnswer = ans,
                    correctAnswer = q.correctAnswer,
                    isCorrect = ans == q.correctAnswer,
                    timeSpentSeconds = state.timePerQuestionSeconds[idx] ?: 0L
                )
            }
            quizDao.insertQuestions(questionEntities)

            state.questions.forEachIndexed { idx, q ->
                val ans = state.answers[idx] ?: -1
                val isCorrect = ans == q.correctAnswer
                val existing = statDao.getStat(q.canonicalKey) ?: QuestionStatEntity(
                    canonicalKey = q.canonicalKey,
                    factorA = minOf(q.factorA, q.factorB),
                    factorB = maxOf(q.factorA, q.factorB)
                )
                statDao.upsertStat(
                    existing.copy(
                        totalAsked = existing.totalAsked + 1,
                        correctCount = existing.correctCount + if (isCorrect) 1 else 0,
                        wrongCount = existing.wrongCount + if (isCorrect) 0 else 1,
                        lastWasWrong = !isCorrect,
                        lastWrongAnswer = if (!isCorrect) ans else existing.lastWrongAnswer,
                        lastAskedTimestamp = now
                    )
                )
            }

            val updated = pref.copy(
                bestPercentage = max(pref.bestPercentage, summary.percentage),
                maxStreakEver = max(pref.maxStreakEver, summary.maxStreak),
                totalCompletedQuizzes = pref.totalCompletedQuizzes + 1,
                incompleteQuizJson = null
            )
            prefDao.savePreferences(updated)
            updated
        }
}
