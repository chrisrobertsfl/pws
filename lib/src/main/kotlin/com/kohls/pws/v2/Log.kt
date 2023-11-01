package com.kohls.pws.v2

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File

data class Log(private val path: String, private val consumer: (String) -> Unit) {
    // TODO Clean up log files?
    private val logger = LoggerFactory.getLogger(Log::class.java)
    val scope = CoroutineScope(Dispatchers.Default)
    val file: File = File(path).apply {
        parentFile?.mkdirs()
        if (!exists()) {
            createNewFile()
        }
        logger.trace("Writing to log file {}", this)
    }

    fun consumeLines() = scope.launch {
        var lastLineCount = 0
        while (isActive) {
            val allLines = file.readLines()
            if (allLines.size > lastLineCount) {
                allLines.drop(lastLineCount).forEach { consumer(it) }
                lastLineCount = allLines.size
            }
            delay(100)  // Wait before checking for new lines
        }
    }

    fun close() = scope.cancel("LogFile scope cancelled")

}