package com.kohls.base

import com.kohls.base.CriteriaMet.Companion.INITIAL_DELAY
import com.kohls.base.CriteriaMet.Companion.INTERVAL
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis
import java.lang.Thread.sleep
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource.Monotonic.markNow

sealed interface CriteriaMet {
    fun checkCondition(condition: Condition): Boolean
    fun delayFor(duration: Duration) = sleep(duration.inWholeMilliseconds)

    companion object {
        val INITIAL_DELAY = 0.seconds
        val INTERVAL = 500.milliseconds
    }
}

@OptIn(ExperimentalTime::class)

data class TimeFrame(
    val duration: Duration,
    val initialDelay: Duration = INITIAL_DELAY,
    val interval: Duration = INTERVAL
) : CriteriaMet {

    init {
        require(duration.isPositive()) { "Duration must be a positive value -> $duration" }
    }

    private val logger by lazy { LoggerFactory.getLogger(TimeFrame::class.java) }
    private val startTime = markNow()


    override fun checkCondition(condition: Condition): Boolean {
        val endTime = startTime.plus(duration)
        logger.debug("$initialDelay initial delay before checking duration of $duration")
        delayFor(initialDelay)

        while (!endTime.hasPassedNow()) {
            val elapsed = startTime.elapsedNow().inWholeMilliseconds
            logger.debug("Checking condition ${condition.name} at time ${elapsed}ms")
            if (condition.isMet()) return true
            logger.debug("$interval interval delay")
            delayFor(interval)
        }
        return false
    }
}

class Eventually(val name: String? = null, private val condition: () -> Boolean) {
    fun isMetWithin(criteria: CriteriaMet, exception: Exception? = null): Boolean {
        val result = criteria.checkCondition(BasicCondition(name = name, met = condition))
        if (!result && exception != null) throw exception
        return result
    }
}

 interface Condition {
     val name : String?
    fun isMet() : Boolean
}
open class BasicCondition(override val name: String?, val met: () -> Boolean) : Condition {
    override fun isMet(): Boolean = met()
}

