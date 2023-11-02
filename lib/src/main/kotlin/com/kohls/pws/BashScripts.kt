package com.kohls.pws

import org.slf4j.LoggerFactory
import java.io.File

data class BashScript(private val body: String, private val variables: Map<String, String>, private val background: Boolean) {
    private val logger = LoggerFactory.getLogger(BashScript::class.java)
    private val file: File = createExecutableFile()
    private val log: Log = Log(path = "/tmp/logs/script-${System.currentTimeMillis()}.log", consumer = { println(it) })

    fun generateContents(): String = buildString {
        appendWithNewLine("#!/bin/bash")
        variables.map { "export ${it.key}=\"${it.value}\"" }.forEach { appendWithNewLine(it) }
        appendWithNewLine(body)
    }

    fun execute(vararg args: Any): Int = execute(args.toList())
    fun execute(args: List<Any>): Int {
        return initialize().run {
            logExecutionDetails()
            runCatching {
                executeProcess(buildCommandLine(args))
            }.getOrElse {
                logger.error("Error executing script", it)
                -1
            }.also {
                log.close()
            }
        }
    }

    private fun logExecutionDetails() {
        logger.info("Executing script: ${file.path}")
        logger.info("Writing to log: ${log.file}")
    }

    private fun executeProcess(commandLine: List<String>): Int {
        val process = ProcessBuilder(commandLine).inheritIO().redirectOutput(ProcessBuilder.Redirect.appendTo(log.file)).redirectErrorStream(true).start()

        log.consumeLines()

        if (background) return 0

        val exitCode = process.waitFor()
        if (process.isAlive) process.destroy()
        return exitCode
    }


    fun initialize() = apply { logFile().writeText("") }

    fun logFile(): File = log.file
    fun logContents(): List<String> = logFile().readLines()
    private fun StringBuilder.appendWithNewLine(string: String) = this.append("$string\n")
    private fun createExecutableFile(): File = kotlin.io.path.createTempFile().toFile().apply {
        writeText(generateContents())
        makeExecutable()
    }

    private fun File.makeExecutable(): Boolean = setExecutable(true)
    private fun buildCommandLine(args: List<Any>): List<String> = listOf(file.path) + args.map { it.toString() }

}

object BashScriptFactory {

    private val NONE = mapOf<String, String>()
    private const val OFF = false
    fun fromLines(
        lines: List<String>, variables: Map<String, String> = NONE, background: Boolean = OFF
    ): BashScript {
        return fromContents(lines.joinToString("\n"), variables, background)
    }

    fun fromResource(
        resourcePath: String, variables: Map<String, String> = NONE, background: Boolean = OFF
    ): BashScript {
        val resourceContents = BashScript::class.java.getResource(resourcePath)?.readText() ?: throw IllegalArgumentException("Resource path $resourcePath could not be read")
        return fromContents(resourceContents, variables, background)
    }

    fun fromContents(
        contents: String, variables: Map<String, String> = NONE, background: Boolean = OFF
    ): BashScript {
        return BashScript(contents, variables, background)
    }
}