package com.kohls.pws

import org.slf4j.LoggerFactory.getLogger
import java.io.File
import kotlin.io.path.createTempFile

data class BashScript(
    private val contents: String,
    private val background: Boolean,
    private val environmentVariables: Map<String, String>,
    private val declaredVariables: Map<String, String>,
    private val consumer: (String) -> Unit = CONSUMER
) {
    private val logger = getLogger(BashScript::class.java)
    private val file: File = createExecutableFile()
    private val log: Log = Log(path = "/tmp/logs/script-${System.currentTimeMillis()}.log", consumer = consumer)

    fun contents(): String = file.readText()

    // TODO Prefix name for createTempFile?
    // TODO How do I clean this up at the end of the day?
    private fun createExecutableFile(): File = createTempFile().toFile().apply {
        writeText(renderContents())
        makeExecutable()
    }


    private fun renderContents(): String {
        val renderedContents = mutableListOf("#!/bin/bash").apply {
            this += environmentVariables.map { it.asExportAssignment() }
            this += declaredVariables.map { it.asDeclareAssignment() }
            this += contents
        }
        return renderedContents.joinToString("\n")
    }

    private fun Map.Entry<String, String>.asDeclareAssignment() = asAssignment("declare")
    private fun Map.Entry<String, String>.asExportAssignment() = asAssignment("export")
    private fun Map.Entry<String, String>.asAssignment(declaration: String) = """
        $declaration ${this.key}="${this.value}"
    """.trimIndent()


    private fun File.makeExecutable(): Boolean = setExecutable(true)
    fun execute(args: List<Any> = listOf()): Int {
        logger.info("Executing script :  ${file.path}")
        logger.info("Writing to log   :  ${log.file}")
        return try {
            val process = ProcessBuilder(buildCommandLine(args)).redirectOutput(ProcessBuilder.Redirect.appendTo(log.file)).redirectErrorStream(true).start()
            log.consumeLines()
            if (background) return 0
            val exitCode = process.waitFor()
            if (process.isAlive) process.destroy()
            exitCode
        } catch (e: Exception) {
            logger.error("Error executing script", e)
            -1
        } finally {
            log.close()
        }
    }

    private fun buildCommandLine(args: List<Any>): List<String> = listOf(file.path) + args.map { it.toString() }
    fun logContents() = log.file.readLines()
    fun logFile(): File = log.file

    companion object {
        private val CONSUMER: (String) -> Unit = { println(it) }
        fun from(
            resourcePath: String,
            background: Boolean = false,
            environmentVariables: Map<String, String> = mapOf(),
            declaredVariables: Map<String, String> = mapOf(),
            consumer: (String) -> Unit = CONSUMER
        ): BashScript {
            println("environmentVariables = ${environmentVariables}")
            return BashScript(
                contents = BashScript::class.java.getResource(resourcePath)?.readText() ?: throw IllegalArgumentException("Resource path $resourcePath could not be read"),
                background = background,
                environmentVariables = environmentVariables,
                declaredVariables = declaredVariables,
                consumer = consumer
            )
        }

        fun from(
            lines: List<String>,
            background: Boolean = false,
            environmentVariables: Map<String, String> = mapOf(),
            declaredVariables: Map<String, String> = mapOf(),
            consumer: (String) -> Unit = CONSUMER
        ) = BashScript(
            contents = lines.joinToString(separator = "\n"), background = background, environmentVariables = environmentVariables, declaredVariables = declaredVariables, consumer = consumer
        )
    }
}