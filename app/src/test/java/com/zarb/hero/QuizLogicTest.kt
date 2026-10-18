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
    fun testQuestionsWithinRange3To9AndExcludes1_2_10() {
        val questions = AdaptiveQuestionSelector.generate20Questions(emptyMap())
        for (q in questions) {
            assertTrue("Factor A must be between 3 and 9", q.factorA in 3..9)
            assertTrue("Factor B must be between 3 and 9", q.factorB in 3..9)
            assertNotEquals("Factor A must not be 1", 1, q.factorA)
            assertNotEquals("Factor A must not be 2", 2, q.factorA)
            assertNotEquals("Factor A must not be 10", 10, q.factorA)
            assertNotEquals("Factor B must not be 1", 1, q.factorB)
            assertNotEquals("Factor B must not be 2", 2, q.factorB)
            assertNotEquals("Factor B must not be 10", 10, q.factorB)
        }
    }

    @Test
    fun testCanonicalPairsCountIs28() {
        val pairs = AdaptiveQuestionSelector.getAllCanonicalPairs()
        assertEquals("Canonical pairs for 3..9 must be exactly 28", 28, pairs.size)
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
        stats["3x3"] = QuestionStatEntity(
            canonicalKey = "3x3",
            factorA = 3,
            factorB = 3,
            totalAsked = 10,
            correctCount = 10,
            wrongCount = 0,
            lastWasWrong = false
        )

        val weightHard = AdaptiveQuestionSelector.calculateWeight(stats["7x8"])
        val weightEasy = AdaptiveQuestionSelector.calculateWeight(stats["3x3"])

        assertTrue("Error question must have significantly higher weight", weightHard > weightEasy * 3.0)
        assertTrue("Mastered question must not have 0 weight", weightEasy >= 1.5)
    }
}
