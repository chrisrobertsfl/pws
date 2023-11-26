package com.kohls.base

import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
data class TimeFrame(
    val duration: Duration,
    val initialDelay: Duration = Criteria.INITIAL_DELAY,
    val interval: Duration = Criteria.INTERVAL
) : Criteria {

    init {
        require(duration.isPositive()) { "Duration must be a positive value -> $duration" }
    }

    private val logger by lazy { LoggerFactory.getLogger(TimeFrame::class.java) }
    private val startTime = TimeSource.Monotonic.markNow()


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