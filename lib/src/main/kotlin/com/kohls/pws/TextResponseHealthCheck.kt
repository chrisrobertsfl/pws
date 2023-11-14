package com.kohls.pws

import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class TextResponseHealthCheck(
    override val name: String = generateName(),
    var url: String = emptyString(),
    var searchedText: String = emptyString(),
    var ignoreCase: Boolean = true,
    var attempts: Int = 10,
    var interval: Duration = 3.seconds,
    var initialDelay : Duration = 3.seconds
) : Action {
    private val logger by lazy { LoggerFactory.getLogger(Maven::class.java) }

    override fun perform(parameters: Parameters): Parameters {
        logger.trace("parameters are {}", parameters)
        ServiceHealthCheck(
            url = url,
            responsePredicate = ResponsePredicate.TextBasedPredicate(searchedText = searchedText, ignoreCase = ignoreCase),
            attempts = attempts,
            interval = interval,
            initialDelay = initialDelay
        ).checkHealth()
        return Parameters.EMPTY
    }
}