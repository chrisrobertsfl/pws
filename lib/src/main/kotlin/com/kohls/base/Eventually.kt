package com.kohls.base

import com.kohls.base.CriteriaMet.Companion.INITIAL_DELAY
import com.kohls.base.CriteriaMet.Companion.INTERVAL
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis
import java.lang.Thread.sleep
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

sealed interface CriteriaMet {
    fun checkCondition(condition: Condition): Boolean
    fun delayFor(duration: Duration) = sleep(duration.inWholeMilliseconds)

    companion object {
        val INITIAL_DELAY = 0.seconds
        val INTERVAL = 500.milliseconds
    }
}

data class NumberOfAttempts(val attempts: Int, val initialDelay: Duration = INITIAL_DELAY, val interval: Duration = INTERVAL) : CriteriaMet {
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

data class TimeFrame(val duration: Duration, val initialDelay: Duration = INITIAL_DELAY, val interval: Duration = INTERVAL) : CriteriaMet {

    private val logger by lazy { LoggerFactory.getLogger(NumberOfAttempts::class.java) }

    override fun checkCondition(condition: Condition): Boolean {
        val endTime = currentTimeMillis() + duration.inWholeMilliseconds
        logger.debug("$initialDelay initial delay before checking duration for $duration")
        delayFor(initialDelay)

        while (currentTimeMillis() < endTime) {
            logger.debug("Checking condition ${condition.name ?: ""}")
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

