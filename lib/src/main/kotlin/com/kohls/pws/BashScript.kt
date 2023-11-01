package com.kohls.pws

import org.slf4j.LoggerFactory.getLogger
import java.io.File
import kotlin.io.path.createTempFile

/**
 * TODO:  Observations
 * The temporary script files created by createExecutableFile() are not explicitly cleaned up.
 * The function from in the companion object could throw an IllegalArgumentException if the resource cannot be found, which is fine, but the caller must be aware of this.
 * The execute method contains the main logic for process execution, which involves starting the process, consuming its lines, and potentially waiting for it to complete. The catch block logs the exception but returns -1 as a generalized error code.
 *
 * Suggestions
 * For cleaning up temporary files, you could maintain a list of them and provide a method to delete them when they're no longer needed.
 * A null-safety check could be added when reading resources.
 * You might consider breaking down the execute() method into smaller parts for better readability and maintenance.
 */
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


    private fun renderContents() = with(mutableListOf("#!/bin/bash")) {
        this += environmentVariables.map { it.asExportAssignment() }
        this += declaredVariables.map { it.asDeclareAssignment() }
        this += contents
        joinToString("\n")
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

            val processBuilder = ProcessBuilder(buildCommandLine(args))
            val env = processBuilder.environment() // Get the environment from the ProcessBuilder
            env.forEach { logger.debug("\tenv['$it.key']=${it.value}")}
            val process = processBuilder
                .inheritIO()
                .redirectOutput(ProcessBuilder.Redirect.appendTo(log.file))
                .redirectErrorStream(true)
                .start()
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