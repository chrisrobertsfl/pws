package com.kohls.pws

import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import com.kohls.pws.ResponsePredicate.JsonBasedPredicate
data class JsonResponseHealthCheck(
    override val name: String = generateName(),
    var url: String = emptyString(),
    var searchedField: Pair<String,String>? = null,
    var ignoreCase: Boolean = true,
    var attempts: Int = 10,
    var interval: Duration = 3.seconds,
    var initialDelay : Duration = 3.seconds
) : Action {
    private val logger by lazy { LoggerFactory.getLogger(Maven::class.java) }

    override fun perform(parameters: Parameters): Parameters {
        logger.trace("parameters are {}", parameters)
        val responsePredicate = JsonBasedPredicate(
            searchedField = requireNotNull(searchedField) { "Missing searchedField" },
            ignoreCase = ignoreCase)
        ServiceHealthCheck(
            name = name,
            url = url,
            responsePredicate = responsePredicate,
            attempts = attempts,
            interval = interval,
            initialDelay = initialDelay
        ).checkHealth()
        return Parameters.EMPTY
    }
}