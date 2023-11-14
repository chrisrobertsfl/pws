package com.kohls.pws

import com.kohls.base.Eventually
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class LogFileEventuallyContains(override val name: String = generateName(), var duration: Duration = 10.seconds, var searchedText: String = emptyString(), var ignoreCase: Boolean = true) : Action {

    private val logger by lazy { LoggerFactory.getLogger(LogFileEventuallyContains::class.java) }

    override fun perform(parameters: Parameters): Parameters {
        logger.trace("parameters are {}", parameters)
        val logFile = parameters.getOrThrow<LogFile>("logFile")
        logger.trace("looking for '$searchedText' in $logFile")
        val condition = { logFile.validate { it.readText().contains(searchedText, ignoreCase = ignoreCase) } }
        val eventually = Eventually(condition = condition)
        val message = "Could not find '${searchedText}' in ${logFile.fullPath()} within $duration"
        eventually.withinDurationOrThrow(duration = duration, message = message)
        return Parameters.EMPTY
    }
}