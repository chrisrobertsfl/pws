package com.kohls.pws

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

data class Eventually(
    val condition: () -> Boolean
) {

    fun satisfiedWithin(duration: Duration, interval: Duration = 500.milliseconds): Boolean {
        val end = System.currentTimeMillis() + duration.inWholeMilliseconds

        while (System.currentTimeMillis() < end) {
            if (condition()) {
                return true
            }
            Thread.sleep(interval.inWholeMilliseconds)
        }
        return false
    }
}