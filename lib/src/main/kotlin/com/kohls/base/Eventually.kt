package com.kohls.base

import com.kohls.base.CriteriaMet.Companion.INITIAL_DELAY
import com.kohls.base.CriteriaMet.Companion.INTERVAL
import java.lang.System.currentTimeMillis
import java.lang.Thread.sleep
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

sealed interface CriteriaMet {
    fun checkCondition(condition: () -> Boolean): Boolean
    fun delayFor(duration: Duration) = sleep(duration.inWholeMilliseconds)

    companion object {
        val INITIAL_DELAY = 0.seconds
        val INTERVAL = 500.milliseconds
    }
}

data class NumberOfAttempts(val attempts: Int, val initialDelay: Duration = INITIAL_DELAY, val interval: Duration = INTERVAL) : CriteriaMet {
    override fun checkCondition(condition: () -> Boolean): Boolean {
        var conditionIsMet: Boolean

        delayFor(initialDelay)
        repeat(attempts) {
            conditionIsMet = condition()
            if (conditionIsMet) return true
            delayFor(interval)
        }

        return false
    }
}

data class TimeFrame(val duration: Duration, val initialDelay: Duration = INITIAL_DELAY, val interval: Duration = INTERVAL) : CriteriaMet {
    override fun checkCondition(condition: () -> Boolean): Boolean {
        val endTime = currentTimeMillis() + duration.inWholeMilliseconds
        delayFor(initialDelay)

        while (currentTimeMillis() < endTime) {
            if (condition()) return true
            delayFor(interval)
        }

        return false
    }
}

class Eventually(private val condition: () -> Boolean) {

    fun isMetWithin(criteria: CriteriaMet, exception: Exception? = null): Boolean {
        val result = criteria.checkCondition(condition)

        if (!result && exception != null) {
            throw exception
        }
        return result
    }


}
