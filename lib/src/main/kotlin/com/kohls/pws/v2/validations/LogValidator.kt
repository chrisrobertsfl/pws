package com.kohls.pws.v2.validations

import com.kohls.pws.v2.Eventually
import com.kohls.pws.v2.Validation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class LogValidator(val duration: Duration, val contains: List<String> = mutableListOf()) : Validation {
    private val logger: Logger = LoggerFactory.getLogger(LogValidator::class.java)

    // TODO Validation:  arg size must be one and of type File
    // TODO Validation on logFile presence

    override fun validate(args: Map<String, Any>): Boolean {
        val logFile = args["logFile"] as File
        val satisfiedWithin = Eventually(condition = {
            val result = contains.all { it in logFile.readText() }
            logger.info("Result is $result:  Checked contents for ${contains}")
            result
        }).satisfiedWithin(duration)
        logger.info("satisfiedWithin is $satisfiedWithin")
        return satisfiedWithin
    }

    class Builder {
        var duration: Duration = 30.seconds
        val contains: MutableList<String> = mutableListOf()
        fun build(): LogValidator = LogValidator(duration = duration, contains = contains)
        fun contains(text: String) {
            this.contains += text
        }
    }
}