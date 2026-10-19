package com.zarb.hero

import com.zarb.hero.data.local.QuestionStatEntity
import com.zarb.hero.data.local.QuizEntity
import com.zarb.hero.data.local.QuizQuestionEntity
import com.zarb.hero.engine.AdaptiveQuestionSelector
import com.zarb.hero.engine.DailyReportBuilder
import com.zarb.hero.engine.QuizScoring
import com.zarb.hero.engine.TableMasteryCalculator
import com.zarb.hero.model.ActiveQuizState
import com.zarb.hero.model.Question
import org.junit.Assert.*
import org.junit.Test
import java.util.TimeZone
import kotlin.random.Random

class QuizLogicTest {

    @Test
    fun testExact20QuestionsGenerated() {
        val questions = AdaptiveQuestionSelector.generate20Questions(emptyMap())
        assertEquals(QuizScoring.QUIZ_SIZE, questions.size)
    }

    @Test
    fun testQuestionsWithinRange3To9() {
        repeat(50) {
            for (q in AdaptiveQuestionSelector.generate20Questions(emptyMap())) {
                assertTrue("Factor A must be between 3 and 9", q.factorA in 3..9)
                assertTrue("Factor B must be between 3 and 9", q.factorB in 3..9)
            }
        }
    }

    @Test
    fun testCanonicalPairsCountIs28() {
        val pairs = AdaptiveQuestionSelector.getAllCanonicalPairs()
        assertEquals("Canonical pairs for 3..9 must be exactly 28", 28, pairs.size)
        assertEquals(28, pairs.toSet().size)
    }

    @Test
    fun testNoDuplicateEquivalentQuestionsInSingleQuiz() {
        repeat(50) {
            val questions = AdaptiveQuestionSelector.generate20Questions(emptyMap())
            val uniqueKeys = questions.map { it.canonicalKey }.toSet()
            assertEquals("No two equivalent questions in one quiz", 20, uniqueKeys.size)
        }
    }

    @Test
    fun testCanonicalKeyMatchesBetweenQuestionAndSelector() {
        val validKeys = AdaptiveQuestionSelector.getAllCanonicalPairs().map { "${it.first}x${it.second}" }.toSet()
        for (q in AdaptiveQuestionSelector.generate20Questions(emptyMap())) {
            assertTrue("Unknown key ${q.canonicalKey}", q.canonicalKey in validKeys)
        }
        assertEquals(Question(3, 7).canonicalKey, Question(7, 3).canonicalKey)
    }

    @Test
    fun testPercentageUsesRealScoringCode() {
        val testCases = listOf(20 to 100, 16 to 80, 10 to 50, 0 to 0, 15 to 75, 19 to 95)
        for ((correct, expectedPct) in testCases) {
            assertEquals(expectedPct, QuizScoring.percentage(correct))
        }
    }

    @Test
    fun testSummarizeCountsCorrectAndMistakes() {
        val questions = listOf(Question(3, 4), Question(7, 8), Question(9, 9))
        val state = ActiveQuizState(
            quizId = 1L,
            questions = questions,
            answers = mapOf(0 to 12, 1 to 54, 2 to 81),
            totalActiveSeconds = 30L,
            maxStreak = 1
        )
        val summary = QuizScoring.summarize(state)
        assertEquals(2, summary.correctCount)
        assertEquals(1, summary.wrongCount)
        assertEquals(3, summary.totalQuestions)
        assertEquals(66, summary.percentage)
        assertEquals(1, summary.mistakes.size)
        assertEquals(54, summary.mistakes[0].childAnswer)
        assertEquals(56, summary.mistakes[0].correctAnswer)
    }

    @Test
    fun testUnansweredQuestionIsCountedAsMistake() {
        val state = ActiveQuizState(quizId = 2L, questions = listOf(Question(3, 3)), answers = emptyMap())
        val summary = QuizScoring.summarize(state)
        assertEquals(0, summary.correctCount)
        assertEquals(-1, summary.mistakes[0].childAnswer)
    }

    @Test
    fun testAdaptiveWeightingIncreasesErrorChance() {
        val hard = QuestionStatEntity(
            canonicalKey = "7x8", factorA = 7, factorB = 8,
            totalAsked = 5, correctCount = 0, wrongCount = 5, lastWasWrong = true
        )
        val easy = QuestionStatEntity(
            canonicalKey = "3x3", factorA = 3, factorB = 3,
            totalAsked = 10, correctCount = 10, wrongCount = 0, lastWasWrong = false
        )
        val weightHard = AdaptiveQuestionSelector.calculateWeight(hard)
        val weightEasy = AdaptiveQuestionSelector.calculateWeight(easy)
        assertTrue("Error question must have significantly higher weight", weightHard > weightEasy * 3.0)
        assertTrue("Mastered question must not have 0 weight", weightEasy >= 1.5)
    }

    @Test
    fun testHardQuestionAppearsMoreOftenThanMasteredQuestion() {
        val stats = mapOf(
            "7x8" to QuestionStatEntity("7x8", 7, 8, totalAsked = 5, correctCount = 0, wrongCount = 5, lastWasWrong = true),
            "3x3" to QuestionStatEntity("3x3", 3, 3, totalAsked = 10, correctCount = 10, wrongCount = 0, lastWasWrong = false)
        )
        val random = Random(1234)
        var hardCount = 0
        var easyCount = 0
        repeat(300) {
            val keys = AdaptiveQuestionSelector.generate20Questions(stats, random).map { it.canonicalKey }
            if ("7x8" in keys) hardCount++
            if ("3x3" in keys) easyCount++
        }
        assertTrue("hard=$hardCount easy=$easyCount", hardCount > easyCount * 2)
        assertTrue("Mastered question must still appear sometimes", easyCount > 0)
    }

    @Test
    fun testDailyUsageReportAggregation() {
        val utc = TimeZone.getTimeZone("UTC")
        val today = 1704888000000L // 2024-01-10 12:00 UTC
        val yesterday = today - 86_400_000L

        val quizzes = listOf(
            QuizEntity(1L, today, 18, 2, 90, 50, 8, true),
            QuizEntity(2L, today, 15, 5, 75, 60, 5, true),
            QuizEntity(3L, yesterday, 20, 0, 100, 40, 20, true)
        )

        val reports = DailyReportBuilder.build(quizzes, utc)

        assertEquals(2, reports.size)
        assertEquals("2024-01-10", reports[0].dateGregorian)
        assertEquals(2, reports[0].quizCount)
        assertEquals(33, reports[0].correctCount)
        assertEquals(7, reports[0].wrongCount)
        assertEquals("2024-01-09", reports[1].dateGregorian)
        assertEquals(1, reports[1].quizCount)
    }

    @Test
    fun testTableMasteryCountsQuestionForBothFactors() {
        fun q(a: Int, b: Int, correct: Boolean, id: Long) = QuizQuestionEntity(
            id = id, quizId = 1L, questionIndex = 1, factorA = a, factorB = b,
            canonicalKey = "${minOf(a, b)}x${maxOf(a, b)}",
            childAnswer = if (correct) a * b else -1, correctAnswer = a * b,
            isCorrect = correct, timeSpentSeconds = 2L
        )

        val questions = listOf(q(3, 7, true, 1), q(7, 8, false, 2), q(7, 7, true, 3))
        val result = TableMasteryCalculator.calculate(questions).associateBy { it.tableNumber }

        assertEquals(7, result.size)
        assertEquals(3, result.getValue(7).totalAnswered) // 3x7, 7x8, 7x7
        assertEquals(2, result.getValue(7).correctCount)
        assertEquals(1, result.getValue(3).totalAnswered)
        assertEquals("داده ناکافی", result.getValue(3).statusText)
    }
}
