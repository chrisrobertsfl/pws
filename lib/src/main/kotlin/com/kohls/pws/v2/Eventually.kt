package com.kohls.pws.v2


import java.lang.System.currentTimeMillis
import java.lang.Thread.sleep
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class Eventually(
    val condition: () -> Boolean
) {
    fun satisfiedWithin(duration: Duration, interval: Duration = 500.milliseconds): Boolean {
        val end = currentTimeMillis() + duration.inWholeMilliseconds

        while (currentTimeMillis() < end) {
            if (condition()) {
                return true
            }
            sleep(interval.inWholeMilliseconds)
        }
        return false
    }
}