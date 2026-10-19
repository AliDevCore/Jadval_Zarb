package com.zarb.hero

import com.zarb.hero.engine.ActiveQuizCodec
import com.zarb.hero.engine.AdaptiveQuestionSelector
import com.zarb.hero.model.ActiveQuizState
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class ActiveQuizCodecTest {

    private fun sampleState(submitted: Boolean): ActiveQuizState {
        val questions = AdaptiveQuestionSelector.generate20Questions(emptyMap(), Random(7))
        return ActiveQuizState(
            quizId = 1234567890123L,
            questions = questions,
            currentIndex = 5,
            answers = mapOf(0 to 12, 1 to 56, 4 to 99),
            timePerQuestionSeconds = mapOf(0 to 3L, 1 to 4L, 4 to 9L),
            totalActiveSeconds = 16L,
            currentStreak = 2,
            maxStreak = 3,
            isAnswerSubmitted = submitted,
            lastSubmittedAnswer = if (submitted) 42 else null
        )
    }

    @Test
    fun roundTripKeepsEverything_submittedState() {
        val original = sampleState(submitted = true)
        val decoded = ActiveQuizCodec.decode(ActiveQuizCodec.encode(original))
        assertEquals(original, decoded)
    }

    @Test
    fun roundTripKeepsEverything_unsubmittedState() {
        val original = sampleState(submitted = false)
        val decoded = ActiveQuizCodec.decode(ActiveQuizCodec.encode(original))
        assertEquals(original, decoded)
        assertNull(decoded?.lastSubmittedAnswer)
    }

    @Test
    fun garbageInputReturnsNull() {
        assertNull(ActiveQuizCodec.decode("not json at all"))
        assertNull(ActiveQuizCodec.decode(""))
        assertNull(ActiveQuizCodec.decode("{}"))
    }

    @Test
    fun wrongQuestionCountReturnsNull() {
        val broken = sampleState(false).copy(questions = sampleState(false).questions.take(5))
        assertNull(ActiveQuizCodec.decode(ActiveQuizCodec.encode(broken)))
    }

    @Test
    fun outOfRangeIndexReturnsNull() {
        val broken = sampleState(false).copy(currentIndex = 20)
        assertNull(ActiveQuizCodec.decode(ActiveQuizCodec.encode(broken)))
    }
}
