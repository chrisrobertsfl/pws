package com.kohls.pws

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File

data class Log(private val path: String, private val consumer: (String) -> Unit) {
    // TODO Clean up log files?
    private val logger = LoggerFactory.getLogger(Log::class.java)
    private val scope = CoroutineScope(Dispatchers.Default)
    val file: File = File(path).apply {
        parentFile?.mkdirs()
        if (!exists()) {
            createNewFile()
        }
        logger.trace("Writing to log file {}", this)
    }

    fun consumeLines() {
        scope.launch {
            file.bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    consumer(line!!)
                    delay(100)  // Wait before checking for new lines
                }
            }
        }
    }

    fun close() {
        scope.cancel("LogFile scope cancelled")
    }
}