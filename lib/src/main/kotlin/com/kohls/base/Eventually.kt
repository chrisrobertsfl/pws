package com.kohls.base

import com.kohls.pws.GitCheckout
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class Eventually(val condition: () -> Boolean) {
    private val logger by lazy { LoggerFactory.getLogger(Eventually::class.java) }

    fun satisfiedWithinDuration(duration: Duration, interval: Duration = 500.milliseconds): Boolean {
        val end = System.currentTimeMillis() + duration.inWholeMilliseconds

        while (System.currentTimeMillis() < end) {
            if (condition()) {
                return true
            }
            sleep(interval.inWholeMilliseconds)
        }
        return false
    }

    fun satisifiedWithinAttempts(attempts: Int, interval: Duration = 500.milliseconds): Boolean {
        logger.trace("Start checking condition for $attempts attempts")
        repeat(attempts) {
            logger.trace("Checking condition at attempt ${it + 1} of $attempts (interval is $interval)")
            if (condition()) {
                return true
            }
            sleep(interval.inWholeMilliseconds)
        }
        return false
    }

    fun satisfiedWithinDurationOrThrow(duration: Duration, interval: Duration = 500.milliseconds, message: String) {
        if (!satisfiedWithinDuration(duration, interval)) {
            throw IllegalStateException(message)
        }
    }

    fun satisfiedWithinAttemptsOrThrow(attempts: Int, interval: Duration = 500.milliseconds, message: String) {
        if (!satisifiedWithinAttempts(attempts, interval)) {
            throw IllegalStateException(message)
        }
    }
}
