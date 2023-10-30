package com.kohls.pws

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import org.slf4j.LoggerFactory
import org.slf4j.LoggerFactory.getLogger
import java.io.File

data class Log(private val path: String, private val consumer: (String) -> Unit) {
    // TODO Clean up log files?
    private val logger = getLogger(Log::class.java)
    private val scope = CoroutineScope(Default)
    val file: File = File(path).apply {
        parentFile?.mkdirs()
        if (!exists()) {
            createNewFile()
        }
        logger.trace("Writing to log file {}", this)
    }

    fun consumeLines() = scope.launch {
        file.bufferedReader().use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                consumer(line!!)
                delay(100)  // Wait before checking for new lines
            }
        }

    }

    fun close() = scope.cancel("LogFile scope cancelled")

}