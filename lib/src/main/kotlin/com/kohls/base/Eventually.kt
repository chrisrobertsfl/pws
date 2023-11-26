package com.kohls.base

import java.lang.Thread.sleep
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

sealed interface Criteria {
    fun checkCondition(condition: Condition): Boolean
    fun delayFor(duration: Duration) = sleep(duration.inWholeMilliseconds)

    companion object {
        val INITIAL_DELAY = 0.seconds
        val INTERVAL = 500.milliseconds
    }
}

class Eventually(val name: String? = null, private val condition: () -> Boolean) {
    fun isMetWithin(criteria: Criteria, exception: Exception? = null): Boolean {
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

