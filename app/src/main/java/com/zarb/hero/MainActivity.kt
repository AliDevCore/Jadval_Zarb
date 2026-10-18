package com.zarb.hero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.zarb.hero.data.local.*
import com.zarb.hero.engine.AdaptiveQuestionSelector
import com.zarb.hero.engine.SoundManager
import com.zarb.hero.model.*
import com.zarb.hero.ui.screens.*
import kotlinx.coroutines.launch
import kotlin.math.max

enum class Screen { HOME, QUIZ, REPORT, REVIEW, HISTORY }

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getInstance(this)
        soundManager = SoundManager(this)

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    var currentScreen by remember { mutableStateOf(Screen.HOME) }

                    var bestPercentage by remember { mutableIntStateOf(0) }
                    var maxStreakEver by remember { mutableIntStateOf(0) }
                    var isSoundOn by remember { mutableStateOf(true) }
                    var activeQuizState by remember { mutableStateOf<ActiveQuizState?>(null) }
                    var completedSummary by remember { mutableStateOf<QuizSummary?>(null) }

                    val completedQuizzes by db.quizDao().getAllCompletedQuizzes().collectAsState(initial = emptyList())

                    LaunchedEffect(Unit) {
                        val pref = db.appPreferenceDao().getPreferencesSync()
                        if (pref != null) {
                            bestPercentage = pref.bestPercentage
                            maxStreakEver = pref.maxStreakEver
                            isSoundOn = pref.soundEnabled
                            soundManager.isSoundEnabled = pref.soundEnabled
                        }
                    }

                    when (currentScreen) {
                        Screen.HOME -> {
                            HomeScreen(
                                bestPercentage = bestPercentage,
                                maxStreak = maxStreakEver,
                                hasIncompleteQuiz = activeQuizState != null && !activeQuizState!!.isComplete,
                                isSoundOn = isSoundOn,
                                onStartNewQuiz = {
                                    coroutineScope.launch {
                                        val statsList = db.questionStatDao().getAllStats()
                                        val statsMap = statsList.associateBy { it.canonicalKey }
                                        val questions = AdaptiveQuestionSelector.generate20Questions(statsMap)
                                        activeQuizState = ActiveQuizState(questions = questions)
                                        currentScreen = Screen.QUIZ
                                    }
                                },
                                onResumeQuiz = {
                                    if (activeQuizState != null) currentScreen = Screen.QUIZ
                                },
                                onViewHistory = { currentScreen = Screen.HISTORY },
                                onReviewMistakes = { currentScreen = Screen.REVIEW },
                                onToggleSound = {
                                    isSoundOn = !isSoundOn
                                    soundManager.isSoundEnabled = isSoundOn
                                    coroutineScope.launch {
                                        val cur = db.appPreferenceDao().getPreferencesSync() ?: AppPreferenceEntity()
                                        db.appPreferenceDao().savePreferences(cur.copy(soundEnabled = isSoundOn))
                                    }
                                }
                            )
                        }

                        Screen.QUIZ -> {
                            val state = activeQuizState
                            if (state != null) {
                                QuizScreen(
                                    state = state,
                                    soundManager = soundManager,
                                    onAnswerSubmitted = { answer, spentSeconds ->
                                        val q = state.currentQuestion ?: return@QuizScreen
                                        val isCorrect = answer == q.correctAnswer
                                        val newAnswers = state.answers.toMutableMap().apply { put(state.currentIndex, answer) }
                                        val newTimes = state.timePerQuestionSeconds.toMutableMap().apply { put(state.currentIndex, spentSeconds) }
                                        val newStreak = if (isCorrect) state.currentStreak + 1 else 0
                                        val newMaxStreak = max(state.maxStreak, newStreak)

                                        activeQuizState = state.copy(
                                            answers = newAnswers,
                                            timePerQuestionSeconds = newTimes,
                                            totalActiveSeconds = state.totalActiveSeconds + spentSeconds,
                                            currentStreak = newStreak,
                                            maxStreak = newMaxStreak,
                                            isAnswerSubmitted = true,
                                            lastSubmittedAnswer = answer
                                        )
                                    },
                                    onNextQuestion = {
                                        if (state.currentIndex < 19) {
                                            activeQuizState = state.copy(
                                                currentIndex = state.currentIndex + 1,
                                                isAnswerSubmitted = false,
                                                lastSubmittedAnswer = null
                                            )
                                        } else {
                                            var correctCount = 0
                                            val mistakes = mutableListOf<MistakeDetail>()

                                            for (i in 0 until 20) {
                                                val q = state.questions[i]
                                                val ans = state.answers[i] ?: -1
                                                if (ans == q.correctAnswer) {
                                                    correctCount++
                                                } else {
                                                    mistakes.add(MistakeDetail(q.factorA, q.factorB, ans, q.correctAnswer))
                                                }
                                            }

                                            val percentage = (correctCount * 100) / 20
                                            val summary = QuizSummary(
                                                quizId = state.quizId,
                                                correctCount = correctCount,
                                                wrongCount = 20 - correctCount,
                                                percentage = percentage,
                                                totalDurationSeconds = state.totalActiveSeconds,
                                                maxStreak = state.maxStreak,
                                                mistakes = mistakes
                                            )

                                            coroutineScope.launch {
                                                db.quizDao().insertQuiz(
                                                    QuizEntity(
                                                        quizId = summary.quizId,
                                                        timestamp = System.currentTimeMillis(),
                                                        correctCount = summary.correctCount,
                                                        wrongCount = summary.wrongCount,
                                                        percentage = summary.percentage,
                                                        durationSeconds = summary.totalDurationSeconds,
                                                        maxStreak = summary.maxStreak,
                                                        isCompleted = true
                                                    )
                                                )

                                                val qEntities = state.questions.mapIndexed { idx, q ->
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
                                                db.quizDao().insertQuestions(qEntities)

                                                for (idx in 0 until 20) {
                                                    val q = state.questions[idx]
                                                    val ans = state.answers[idx] ?: -1
                                                    val isCorr = ans == q.correctAnswer
                                                    val existing = db.questionStatDao().getStat(q.canonicalKey)
                                                        ?: QuestionStatEntity(
                                                            canonicalKey = q.canonicalKey,
                                                            factorA = if (q.factorA <= q.factorB) q.factorA else q.factorB,
                                                            factorB = if (q.factorA <= q.factorB) q.factorB else q.factorA
                                                        )

                                                    val updated = existing.copy(
                                                        totalAsked = existing.totalAsked + 1,
                                                        correctCount = existing.correctCount + if (isCorr) 1 else 0,
                                                        wrongCount = existing.wrongCount + if (isCorr) 0 else 1,
                                                        lastWasWrong = !isCorr,
                                                        lastWrongAnswer = if (!isCorr) ans else existing.lastWrongAnswer,
                                                        lastAskedTimestamp = System.currentTimeMillis()
                                                    )
                                                    db.questionStatDao().upsertStat(updated)
                                                }

                                                val pref = db.appPreferenceDao().getPreferencesSync() ?: AppPreferenceEntity()
                                                val newBest = max(pref.bestPercentage, percentage)
                                                val newStreakRecord = max(pref.maxStreakEver, state.maxStreak)
                                                bestPercentage = newBest
                                                maxStreakEver = newStreakRecord

                                                db.appPreferenceDao().savePreferences(
                                                    pref.copy(
                                                        bestPercentage = newBest,
                                                        maxStreakEver = newStreakRecord,
                                                        totalCompletedQuizzes = pref.totalCompletedQuizzes + 1,
                                                        incompleteQuizJson = null
                                                    )
                                                )
                                            }

                                            completedSummary = summary
                                            activeQuizState = null
                                            currentScreen = Screen.REPORT
                                        }
                                    },
                                    onExitQuiz = {
                                        currentScreen = Screen.HOME
                                    }
                                )
                            }
                        }

                        Screen.REPORT -> {
                            val summary = completedSummary
                            if (summary != null) {
                                ReportScreen(
                                    summary = summary,
                                    bestPercentage = bestPercentage,
                                    onBackHome = { currentScreen = Screen.HOME }
                                )
                            }
                        }

                        Screen.REVIEW -> {
                            var mistakeStats by remember { mutableStateOf<List<QuestionStatEntity>>(emptyList()) }
                            LaunchedEffect(Unit) {
                                val all = db.questionStatDao().getAllStats()
                                mistakeStats = all.filter { it.wrongCount > 0 }.sortedByDescending { it.wrongCount }
                            }
                            ReviewMistakesScreen(
                                mistakeStats = mistakeStats,
                                onBackHome = { currentScreen = Screen.HOME }
                            )
                        }

                        Screen.HISTORY -> {
                            var tableMasteries by remember { mutableStateOf<List<TableMastery>>(emptyList()) }
                            LaunchedEffect(completedQuizzes) {
                                val allQuestions = mutableListOf<QuizQuestionEntity>()
                                completedQuizzes.forEach { quiz ->
                                    allQuestions.addAll(db.quizDao().getQuestionsForQuiz(quiz.quizId))
                                }

                                // جداول ۳ تا ۹
                                val list = (3..9).map { num ->
                                    val relevant = allQuestions.filter { it.factorA == num || it.factorB == num }
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
                                tableMasteries = list
                            }

                            HistoryScreen(
                                quizzes = completedQuizzes,
                                tableStats = tableMasteries,
                                onBackHome = { currentScreen = Screen.HOME }
                            )
                        }
                    }
                }
            }
        }
    }
}
