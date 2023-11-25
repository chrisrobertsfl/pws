package com.kohls.base

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock
import kotlin.time.Duration.Companion.seconds

class NumberOfAttemptsTest : FeatureSpec({

    feature("Check Condition") {
        scenario("Zero attempts never meets condition") {
            mock<Condition>().check(0) shouldBe false
        }

        scenario("One attempt meets condition for a count of 1") {
            CountingCondition(count = 1).check(1) shouldBe true
        }

        scenario("Two attempts does not meet condition for a count of 3") {
            CountingCondition(count = 3).check(2) shouldBe false
        }

        scenario("Five attempts meets condition for a count of 4") {
            CountingCondition(count = 4).check(5) shouldBe true
        }
    }
})

private fun Condition.check(attempts: Int) = NumberOfAttempts(attempts = attempts, interval = 0.seconds).checkCondition(this)
data class CountingCondition(val count: Int, override val name: String? = "Met when count is >= $count") : Condition {
    private var attempts = 0
    override fun isMet(): Boolean {
        attempts += 1
        val isMet = attempts == count
        println("$attempts of $count : isMet -> $isMet")
        return isMet
    }
}
