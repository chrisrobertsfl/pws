package com.kohls.base

import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


data class Eventually(val condition: () -> Boolean) {
    private val logger by lazy { LoggerFactory.getLogger(Eventually::class.java) }

    private fun waitForCondition(
        attempts: Int? = null, duration: Duration? = null, interval: Duration = INTERVAL, initialDelay: Duration = INITIAL_DELAY
    ): Boolean {
        sleep(initialDelay.inWholeMilliseconds)
        val endTime = duration?.let { System.currentTimeMillis() + it.inWholeMilliseconds }

        var attemptCount = 0
        while (attempts == null || attemptCount < attempts) {
            if (endTime != null && System.currentTimeMillis() >= endTime) break

            logger.trace("Checking condition at attempt ${attemptCount + 1} of $attempts (interval is $interval)")
            if (condition()) {
                return true
            }
            sleep(interval.inWholeMilliseconds)
            attemptCount++
        }
        return false
    }

    fun withinDuration(duration: Duration, interval: Duration = INTERVAL, initialDelay: Duration = INITIAL_DELAY): Boolean =
        waitForCondition(duration = duration, interval = interval, initialDelay = initialDelay)

    fun withinAttempts(attempts: Int, interval: Duration = INTERVAL, initialDelay: Duration = INITIAL_DELAY): Boolean =
        waitForCondition(attempts = attempts, interval = interval, initialDelay = initialDelay)

    fun withinDurationOrThrow(duration: Duration, interval: Duration = INTERVAL, initialDelay: Duration = INITIAL_DELAY, message: String) {
        if (!withinDuration(duration, interval, initialDelay)) {
            throw IllegalStateException(message)
        }
    }

    fun withinAttemptsOrThrow(attempts: Int, interval: Duration = INTERVAL, initialDelay: Duration = INITIAL_DELAY, message: String) {
        if (!withinAttempts(attempts, interval, initialDelay)) {
            throw IllegalStateException(message)
        }
    }

    companion object {
        val INITIAL_DELAY = 0.seconds

        val INTERVAL = 500.milliseconds
    }
}
