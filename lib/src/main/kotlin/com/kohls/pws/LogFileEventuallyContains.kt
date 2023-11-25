package com.kohls.pws

import com.kohls.base.Eventually
import com.kohls.base.TimeFrame
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

data class LogFileEventuallyContains(override val name: String = generateName(), var duration: Duration = 10.seconds, var initialDelay: Duration = 0.seconds, var interval : Duration = 500.milliseconds, var searchedText: String = emptyString(), var ignoreCase: Boolean = true) : Action {

    private val logger by lazy { LoggerFactory.getLogger(LogFileEventuallyContains::class.java) }
    private val timeFrame by lazy { TimeFrame(duration = duration, initialDelay = initialDelay, interval = interval) }
    override fun perform(parameters: Parameters): Parameters {
        logger.trace("parameters are {}", parameters)
        val logFile = parameters.getOrThrow<LogFile>("logFile")
        if (initialDelay.isPositive()) {
            logger.trace("Initial delay of $initialDelay before checking condition")
        }
        logger.trace("looking for '{}' in {}", searchedText, logFile)
        Eventually(name = name, condition = logFile.hasText()).isMetWithin(criteria = timeFrame, exception = exception(logFile))
        return Parameters.EMPTY
    }

    private fun LogFile.hasText(): () -> Boolean = {
        this.validate { it.readText().contains(searchedText, ignoreCase = ignoreCase) }
    }

    private fun exception(logFile: LogFile) = Exception("Could not find '${searchedText}' in ${logFile.fullPath()} within $duration")
}