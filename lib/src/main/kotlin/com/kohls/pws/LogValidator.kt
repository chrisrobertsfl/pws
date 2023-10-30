package com.kohls.pws

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.time.Duration.Companion.seconds

data class LogValidator(val duration: kotlin.time.Duration, val contains: List<String> = mutableListOf()) : Validation {
    private val logger : Logger = LoggerFactory.getLogger(LogValidator::class.java)

    // TODO Validation:  arg size must be one and of type File
    // TODO Validation on logFile presence

    override fun validate(args : Map<String, Any>) : Boolean {
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
        var duration: kotlin.time.Duration = 30.seconds
        val contains: MutableList<String> = mutableListOf()
        fun build(): LogValidator {
            return LogValidator(duration = duration, contains = contains)
        }
        fun contains(text: String) {
            this.contains += text
        }
    }
}