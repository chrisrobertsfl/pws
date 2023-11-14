package com.kohls.pws

import com.kohls.base.Eventually
import com.kohls.base.Eventually.Companion.INITIAL_DELAY
import khttp.get
import khttp.responses.Response
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import java.net.ConnectException
import kotlin.time.Duration

sealed interface ResponsePredicate {
    fun test(response: Response): Boolean

    data class TextBasedPredicate(private val searchedText: String, private val ignoreCase: Boolean = true) : ResponsePredicate {
        override fun test(response: Response): Boolean = response.text.contains(searchedText, ignoreCase)
    }

    data class JsonBasedPredicate(private val key: String, private val expectedValue: String) : ResponsePredicate {
        override fun test(response: Response): Boolean {
            val json = Json.parseToJsonElement(response.text).jsonObject
            return json[key]?.jsonPrimitive?.content == expectedValue
        }
    }
}

data class ServiceHealthCheck(val url: String, val responsePredicate: ResponsePredicate, val attempts: Int, val interval: Duration, val initialDelay: Duration = INITIAL_DELAY) {
    private val logger by lazy { LoggerFactory.getLogger(Maven::class.java) }

    fun checkHealth() {
        val eventually = Eventually {
            val response = get(url)
            responsePredicate.test(response)
        }
        try {
            eventually.withinAttemptsOrThrow(
                attempts = attempts, interval = interval, initialDelay = initialDelay, message = "Service did not respond with $attempts attempt(s) while checking response: $responsePredicate"
            )
            logger.info("Looks like all is ok with:  $this")
        } catch (connectionException: ConnectException) {
            throw Exception("Service health check connection refused url: $url")
        }
    }
}
