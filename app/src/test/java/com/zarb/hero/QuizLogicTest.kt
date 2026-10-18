package com.zarb.hero

import com.zarb.hero.data.local.QuestionStatEntity
import com.zarb.hero.engine.AdaptiveQuestionSelector
import org.junit.Assert.*
import org.junit.Test

class QuizLogicTest {

    @Test
    fun testExact20QuestionsGenerated() {
        val questions = AdaptiveQuestionSelector.generate20Questions(emptyMap())
        assertEquals(20, questions.size)
    }

    @Test
    fun testQuestionsWithinRange1To10() {
        val questions = AdaptiveQuestionSelector.generate20Questions(emptyMap())
        for (q in questions) {
            assertTrue("Factor A must be between 1 and 10", q.factorA in 1..10)
            assertTrue("Factor B must be between 1 and 10", q.factorB in 1..10)
        }
    }

    @Test
    fun testNoDuplicateEquivalentQuestionsInSingleQuiz() {
        val questions = AdaptiveQuestionSelector.generate20Questions(emptyMap())
        val keys = questions.map { it.canonicalKey }
        val uniqueKeys = keys.toSet()
        assertEquals("No two equivalent questions in one quiz", 20, uniqueKeys.size)
    }

    @Test
    fun testExactPercentageCalculation() {
        val testCases = listOf(
            Pair(20, 100),
            Pair(16, 80),
            Pair(10, 50),
            Pair(0, 0),
            Pair(15, 75),
            Pair(19, 95)
        )
        for ((correct, expectedPct) in testCases) {
            val actual = (correct * 100) / 20
            assertEquals("Percentage calculation failed", expectedPct, actual)
        }
    }

    @Test
    fun testAdaptiveWeightingIncreasesErrorChance() {
        val stats = mutableMapOf<String, QuestionStatEntity>()
        stats["7x8"] = QuestionStatEntity(
            canonicalKey = "7x8",
            factorA = 7,
            factorB = 8,
            totalAsked = 5,
            correctCount = 0,
            wrongCount = 5,
            lastWasWrong = true
        )
        stats["2x2"] = QuestionStatEntity(
            canonicalKey = "2x2",
            factorA = 2,
            factorB = 2,
            totalAsked = 10,
            correctCount = 10,
            wrongCount = 0,
            lastWasWrong = false
        )

        val weightHard = AdaptiveQuestionSelector.calculateWeight(stats["7x8"])
        val weightEasy = AdaptiveQuestionSelector.calculateWeight(stats["2x2"])

        assertTrue("Error question must have significantly higher weight", weightHard > weightEasy * 3.0)
        assertTrue("Mastered question must not have 0 weight", weightEasy >= 1.5)
    }
}
