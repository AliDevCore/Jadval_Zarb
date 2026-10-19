package com.zarb.hero.engine

import com.zarb.hero.model.ActiveQuizState
import com.zarb.hero.model.Question
import org.json.JSONArray
import org.json.JSONObject

/** سریال‌سازی وضعیت آزمون نیمه‌تمام برای ذخیره در دیتابیس (ستون incompleteQuizJson). */
object ActiveQuizCodec {
    private const val VERSION = 1

    fun encode(state: ActiveQuizState): String {
        val root = JSONObject()
        root.put("v", VERSION)
        root.put("quizId", state.quizId)
        root.put("currentIndex", state.currentIndex)
        root.put("totalActiveSeconds", state.totalActiveSeconds)
        root.put("currentStreak", state.currentStreak)
        root.put("maxStreak", state.maxStreak)
        root.put("isAnswerSubmitted", state.isAnswerSubmitted)
        val lastAnswer = state.lastSubmittedAnswer
        if (lastAnswer != null) root.put("lastSubmittedAnswer", lastAnswer)

        val questions = JSONArray()
        state.questions.forEach { q -> questions.put(JSONArray().put(q.factorA).put(q.factorB)) }
        root.put("questions", questions)

        val answers = JSONObject()
        state.answers.forEach { (index, value) -> answers.put(index.toString(), value) }
        root.put("answers", answers)

        val times = JSONObject()
        state.timePerQuestionSeconds.forEach { (index, value) -> times.put(index.toString(), value) }
        root.put("times", times)

        return root.toString()
    }

    /** در صورت خراب یا ناسازگار بودن داده، null برمی‌گرداند (و برنامه از صفر شروع می‌کند). */
    fun decode(json: String): ActiveQuizState? {
        return try {
            val root = JSONObject(json)
            if (root.getInt("v") != VERSION) return null

            val qArray = root.getJSONArray("questions")
            val questions = List(qArray.length()) { i ->
                val pair = qArray.getJSONArray(i)
                Question(pair.getInt(0), pair.getInt(1))
            }
            if (questions.size != QuizScoring.QUIZ_SIZE) return null

            val currentIndex = root.getInt("currentIndex")
            if (currentIndex !in questions.indices) return null

            val answersObj = root.getJSONObject("answers")
            val answers = answersObj.keys().asSequence().associate { key -> key.toInt() to answersObj.getInt(key) }
            val timesObj = root.getJSONObject("times")
            val times = timesObj.keys().asSequence().associate { key -> key.toInt() to timesObj.getLong(key) }

            ActiveQuizState(
                quizId = root.getLong("quizId"),
                questions = questions,
                currentIndex = currentIndex,
                answers = answers,
                timePerQuestionSeconds = times,
                totalActiveSeconds = root.getLong("totalActiveSeconds"),
                currentStreak = root.getInt("currentStreak"),
                maxStreak = root.getInt("maxStreak"),
                isAnswerSubmitted = root.getBoolean("isAnswerSubmitted"),
                lastSubmittedAnswer = if (root.has("lastSubmittedAnswer")) root.getInt("lastSubmittedAnswer") else null
            )
        } catch (e: Exception) {
            null
        }
    }
}
