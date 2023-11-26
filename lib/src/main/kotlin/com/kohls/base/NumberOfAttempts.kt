package com.kohls.base

import org.slf4j.LoggerFactory
import kotlin.time.Duration

data class NumberOfAttempts(val attempts: Int, val initialDelay: Duration = Criteria.INITIAL_DELAY, val interval: Duration = Criteria.INTERVAL) : Criteria {
    private val logger by lazy { LoggerFactory.getLogger(NumberOfAttempts::class.java) }

    override fun checkCondition(condition: Condition): Boolean {
        logger.debug("$initialDelay initial delay")
        var numberOfAttempts: Int = 1
        delayFor(initialDelay)
        repeat(attempts) {
            logger.debug("$numberOfAttempts of $attempts:  Checking condition ${condition.name ?: ""}")
            if (condition.isMet()) return true
            numberOfAttempts += 1
            logger.debug("$interval interval delay")
            delayFor(interval)
        }
        return false
    }
}