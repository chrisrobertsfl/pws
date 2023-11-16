package com.kohls.pws

import com.kohls.base.CriteriaMet.Companion.INITIAL_DELAY
import com.kohls.base.Eventually
import com.kohls.base.NumberOfAttempts
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

    data class JsonBasedPredicate(private val searchedField : Pair<String, String>, private val ignoreCase: Boolean = true) : ResponsePredicate {
        override fun test(response: Response): Boolean {
            val json = Json.parseToJsonElement(response.text).jsonObject
            val actual = json[searchedField.first]?.jsonPrimitive?.content
            val expected = searchedField.second
            return expected.equals(actual, ignoreCase)
        }
    }
}

data class ServiceHealthCheck(val name : String, val url: String, val responsePredicate: ResponsePredicate, val attempts: Int, val interval: Duration, val initialDelay: Duration = INITIAL_DELAY) {
    private val logger by lazy { LoggerFactory.getLogger(ServiceHealthCheck::class.java) }

    fun checkHealth() {
        val eventually = Eventually(name) { responsePredicate.test(get(url)) }
        val exception = Exception("Service did not respond with $attempts attempt(s) while checking response: $responsePredicate")

        try {
            eventually.isMetWithin(NumberOfAttempts(attempts = attempts, initialDelay = initialDelay, interval = interval), exception = exception)
            logger.debug("Looks like all is ok with:  {}", this)
        } catch (connectionException: ConnectException) {
            throw Exception("Service health check connection refused url: $url")
        }
    }
}
