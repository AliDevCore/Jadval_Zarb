package com.zarb.hero

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zarb.hero.data.QuizRepository
import com.zarb.hero.data.local.AppDatabase
import com.zarb.hero.data.local.QuestionStatEntity
import com.zarb.hero.data.local.QuizEntity
import com.zarb.hero.engine.AdaptiveQuestionSelector
import com.zarb.hero.engine.QuizScoring
import com.zarb.hero.engine.TableMasteryCalculator
import com.zarb.hero.model.ActiveQuizState
import com.zarb.hero.model.QuizSummary
import com.zarb.hero.model.TableMastery
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.max

enum class Screen { HOME, QUIZ, REPORT, REVIEW, HISTORY }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuizRepository(AppDatabase.getInstance(application))

    // نوشتن‌های دیتابیس به‌ترتیب انجام می‌شوند تا ذخیره قدیمی روی جدیدتر نیفتد
    private val saveMutex = Mutex()

    var currentScreen by mutableStateOf(Screen.HOME)
        private set
    var activeQuiz by mutableStateOf<ActiveQuizState?>(null)
        private set
    var completedSummary by mutableStateOf<QuizSummary?>(null)
        private set
    var bestPercentage by mutableIntStateOf(0)
        private set
    var maxStreakEver by mutableIntStateOf(0)
        private set
    var isSoundOn by mutableStateOf(true)
        private set
    var mistakeStats by mutableStateOf<List<QuestionStatEntity>>(emptyList())
        private set

    val completedQuizzes: StateFlow<List<QuizEntity>> = repository.completedQuizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tableMasteries: StateFlow<List<TableMastery>> = repository.allQuestions
        .map { TableMasteryCalculator.calculate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            runCatching {
                val pref = repository.loadPreferences()
                bestPercentage = pref.bestPercentage
                maxStreakEver = pref.maxStreakEver
                isSoundOn = pref.soundEnabled
                // بازیابی آزمون نیمه‌تمام بعد از بسته شدن یا از بین رفتن برنامه
                if (activeQuiz == null) {
                    activeQuiz = repository.loadActiveQuiz()
                }
            }
        }
    }

    fun startNewQuiz() {
        viewModelScope.launch {
            val stats = runCatching { repository.loadStats() }.getOrDefault(emptyMap())
            val state = ActiveQuizState(questions = AdaptiveQuestionSelector.generate20Questions(stats))
            activeQuiz = state
            completedSummary = null
            currentScreen = Screen.QUIZ
            persistActive(state)
        }
    }

    fun resumeQuiz() {
        if (activeQuiz != null) currentScreen = Screen.QUIZ
    }

    fun submitAnswer(answer: Int, spentSeconds: Long) {
        val state = activeQuiz ?: return
        if (state.isAnswerSubmitted) return
        val question = state.currentQuestion ?: return

        val isCorrect = answer == question.correctAnswer
        val newStreak = if (isCorrect) state.currentStreak + 1 else 0
        val updated = state.copy(
            answers = state.answers + (state.currentIndex to answer),
            timePerQuestionSeconds = state.timePerQuestionSeconds + (state.currentIndex to spentSeconds),
            totalActiveSeconds = state.totalActiveSeconds + spentSeconds,
            currentStreak = newStreak,
            maxStreak = max(state.maxStreak, newStreak),
            isAnswerSubmitted = true,
            lastSubmittedAnswer = answer
        )
        activeQuiz = updated
        persistActive(updated)
    }

    fun nextQuestion() {
        val state = activeQuiz ?: return
        if (!state.isAnswerSubmitted) return

        if (state.currentIndex < state.questions.size - 1) {
            val updated = state.copy(
                currentIndex = state.currentIndex + 1,
                isAnswerSubmitted = false,
                lastSubmittedAnswer = null
            )
            activeQuiz = updated
            persistActive(updated)
        } else {
            finishQuiz(state)
        }
    }

    private fun finishQuiz(state: ActiveQuizState) {
        val summary = QuizScoring.summarize(state)
        completedSummary = summary
        activeQuiz = null
        currentScreen = Screen.REPORT

        viewModelScope.launch {
            saveMutex.withLock {
                runCatching { repository.saveCompletedQuiz(state, summary) }
                    .onSuccess { pref ->
                        bestPercentage = pref.bestPercentage
                        maxStreakEver = pref.maxStreakEver
                    }
            }
        }
    }

    private fun persistActive(state: ActiveQuizState?) {
        viewModelScope.launch {
            saveMutex.withLock {
                runCatching { repository.saveActiveQuiz(state) }
            }
        }
    }

    /** خروج از آزمون؛ وضعیت قبلاً ذخیره شده و از صفحه اصلی قابل ادامه است. */
    fun exitQuiz() {
        currentScreen = Screen.HOME
    }

    fun goHome() {
        currentScreen = Screen.HOME
    }

    fun openHistory() {
        currentScreen = Screen.HISTORY
    }

    fun openReview() {
        viewModelScope.launch {
            mistakeStats = runCatching { repository.loadMistakeStats() }.getOrDefault(emptyList())
            currentScreen = Screen.REVIEW
        }
    }

    fun toggleSound() {
        val enabled = !isSoundOn
        isSoundOn = enabled
        viewModelScope.launch {
            saveMutex.withLock {
                runCatching { repository.setSoundEnabled(enabled) }
            }
        }
    }
}
