package com.zarb.hero.engine

import com.zarb.hero.data.local.QuestionStatEntity
import com.zarb.hero.model.Question
import kotlin.random.Random

object AdaptiveQuestionSelector {

    // تولید کل ۲۸ عبارت متمایز جدول ضرب اعداد ۳ تا ۹ (حذف ضرایب خیلی ساده ۱، ۲ و ۱۰)
    fun getAllCanonicalPairs(): List<Pair<Int, Int>> {
        val pairs = mutableListOf<Pair<Int, Int>>()
        for (i in 3..9) {
            for (j in i..9) {
                pairs.add(Pair(i, j))
            }
        }
        return pairs // دقیقاً ۲۸ جفت یکتا
    }

    fun calculateWeight(stat: QuestionStatEntity?): Double {
        if (stat == null || stat.totalAsked == 0) {
            return 15.0
        }
        val base = 10.0
        val wrongBonus = stat.wrongCount * 8.0
        val masteryPenalty = stat.correctCount * 1.5
        val recencyBonus = if (stat.lastWasWrong) 12.0 else 0.0

        val computed = base + wrongBonus - masteryPenalty + recencyBonus
        return computed.coerceAtLeast(1.5)
    }

    // انتخاب دقیقاً ۲۰ سؤال بدون تکرار از بین ۲۸ جفت معتبر جدول ضرب ۳ تا ۹
    fun generate20Questions(stats: Map<String, QuestionStatEntity>, random: Random = Random.Default): List<Question> {
        val allPairs = getAllCanonicalPairs()
        val pool = allPairs.toMutableList()
        val weights = pool.map { pair ->
            val key = "${pair.first}x${pair.second}"
            calculateWeight(stats[key])
        }.toMutableList()

        val selectedPairs = mutableListOf<Pair<Int, Int>>()

        repeat(QuizScoring.QUIZ_SIZE) {
            val totalWeight = weights.sum()
            var r = random.nextDouble() * totalWeight
            var chosenIndex = 0
            for (i in weights.indices) {
                r -= weights[i]
                if (r <= 0.0) {
                    chosenIndex = i
                    break
                }
            }

            selectedPairs.add(pool[chosenIndex])
            pool.removeAt(chosenIndex)
            weights.removeAt(chosenIndex)
        }

        return selectedPairs.shuffled(random).map { (a, b) ->
            if (random.nextBoolean()) {
                Question(factorA = a, factorB = b)
            } else {
                Question(factorA = b, factorB = a)
            }
        }
    }
}
