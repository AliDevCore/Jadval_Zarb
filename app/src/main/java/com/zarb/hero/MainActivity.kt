package com.zarb.hero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zarb.hero.engine.SoundManager
import com.zarb.hero.ui.screens.HistoryScreen
import com.zarb.hero.ui.screens.HomeScreen
import com.zarb.hero.ui.screens.QuizScreen
import com.zarb.hero.ui.screens.ReportScreen
import com.zarb.hero.ui.screens.ReviewMistakesScreen

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = SoundManager(this)

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = viewModel()
                    val completedQuizzes by viewModel.completedQuizzes.collectAsState()
                    val tableMasteries by viewModel.tableMasteries.collectAsState()

                    SideEffect { soundManager.isSoundEnabled = viewModel.isSoundOn }

                    when (viewModel.currentScreen) {
                        Screen.HOME -> HomeScreen(
                            bestPercentage = viewModel.bestPercentage,
                            maxStreak = viewModel.maxStreakEver,
                            hasIncompleteQuiz = viewModel.activeQuiz != null,
                            isSoundOn = viewModel.isSoundOn,
                            onStartNewQuiz = viewModel::startNewQuiz,
                            onResumeQuiz = viewModel::resumeQuiz,
                            onViewHistory = viewModel::openHistory,
                            onReviewMistakes = viewModel::openReview,
                            onToggleSound = viewModel::toggleSound
                        )

                        Screen.QUIZ -> {
                            val state = viewModel.activeQuiz
                            if (state != null) {
                                QuizScreen(
                                    state = state,
                                    soundManager = soundManager,
                                    onAnswerSubmitted = viewModel::submitAnswer,
                                    onNextQuestion = viewModel::nextQuestion,
                                    onExitQuiz = viewModel::exitQuiz
                                )
                            }
                        }

                        Screen.REPORT -> {
                            val summary = viewModel.completedSummary
                            if (summary != null) {
                                ReportScreen(
                                    summary = summary,
                                    bestPercentage = viewModel.bestPercentage,
                                    onBackHome = viewModel::goHome
                                )
                            }
                        }

                        Screen.REVIEW -> ReviewMistakesScreen(
                            mistakeStats = viewModel.mistakeStats,
                            onBackHome = viewModel::goHome
                        )

                        Screen.HISTORY -> HistoryScreen(
                            quizzes = completedQuizzes,
                            tableStats = tableMasteries,
                            onBackHome = viewModel::goHome
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        soundManager.release()
        super.onDestroy()
    }
}
