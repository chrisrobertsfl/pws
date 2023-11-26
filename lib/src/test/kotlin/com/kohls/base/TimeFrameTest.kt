package com.kohls.base

import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource.Monotonic.markNow

class TimeFrameTest : FeatureSpec({

    feature("Initialization") {
        scenario("No duration should fail") {
            assertThrows<IllegalArgumentException> { TimeFrame(duration = 0.seconds) }
        }
    }

    feature("Check Condition") {
        scenario("Small duration with condition met") {
            val condition = OnlyAfter(boolean = true)
            val timeFrame = TimeFrame(duration = 100.milliseconds, interval = 0.seconds)
            eventually(250.milliseconds) {
                timeFrame.checkCondition(condition) shouldBe true
            }
        }

        scenario("Small duration with condition not met") {
            val condition = OnlyAfter(boolean = false)
            val timeFrame = TimeFrame(duration = 100.milliseconds, interval = 0.seconds)
            eventually(250.milliseconds) {
                timeFrame.checkCondition(condition) shouldBe false
            }
        }

        scenario("Condition met within allotted duration") {
            val condition = OnlyAfter(duration = 0.5.seconds, boolean = true)
            val timeFrame = TimeFrame(duration = 1.seconds, interval = 0.1.seconds)

            eventually(250.milliseconds) {
                timeFrame.checkCondition(condition) shouldBe true
            }
        }

        scenario("Condition is not met within allotted duration") {
            val condition = OnlyAfter(duration = 1.seconds, boolean = true)
            val timeFrame = TimeFrame(duration = 0.5.seconds, interval = 0.1.seconds)

            eventually(250.milliseconds) {
                timeFrame.checkCondition(condition) shouldBe false
            }
        }

        scenario("Condition is met within allotted duration but should return false") {
            val condition = OnlyAfter(duration = 1.seconds, boolean = false)
            val timeFrame = TimeFrame(duration = 0.5.seconds, interval = 0.1.seconds)

            eventually(250.milliseconds) {
                timeFrame.checkCondition(condition) shouldBe false
            }
        }

    }
})

@OptIn(ExperimentalTime::class)
class OnlyAfter(private val duration: Duration = 1.milliseconds, private val boolean: Boolean = true) : Condition {

    private val startTime = markNow()

    override val name: String = "Only after $duration with condition be $boolean"

    override fun isMet(): Boolean {
        return if (startTime.elapsedNow() >= duration) boolean else false
    }
}