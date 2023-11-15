package com.kohls.pws

import com.kohls.base.Directory
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

interface FileGenerator {
    fun generate(): File

    fun File.touch() = createNewFile()
    fun File.makeExecutable() = this.setExecutable(true)
}


fun interface ScriptRendering {
    fun render(): String
}

data class ScriptFile(private val file: File) {
    fun contents(): String = file.readText()
    fun validate(predicate: (File) -> Boolean): Boolean = predicate(file)
    fun commandLine(args: List<Any>): List<String> = listOf(fullPath()) + args.map { it.toString() }


    fun fullPath(): String = file.path
}

data class LogFile(private val file: File) {
    private val logger = LoggerFactory.getLogger(file.name)

    private val scope = CoroutineScope(Dispatchers.Default)
    fun validate(predicate: (File) -> Boolean): Boolean = predicate(file)
    fun close() = scope.cancel("LogFile scope cancelled")

    fun forAppending(): File = file

    fun consumeLines() = scope.launch {
        var lastLineCount = 0
        while (isActive) {
            val allLines = file.readLines()
            if (allLines.size > lastLineCount) {
                allLines.drop(lastLineCount).forEach {
                    //println(it)
                    logger.trace(it)
                }
                lastLineCount = allLines.size
            }
            delay(100)  // Wait before checking for new lines
        }
    }

    fun fullPath(): String = file.path

}

data class ExecutableScript(val scriptFile: ScriptFile, val logFile: LogFile, val background: Boolean = false, val workingDirectory: Directory? = null) {
    private val logger: Logger = LoggerFactory.getLogger(ExecutableScript::class.java)
    fun scriptContents(): String = scriptFile.contents()

    fun execute(args: List<Any>) = runCatching {
        logger.debug("Script is here :  ${scriptFile.fullPath()}")
        logger.debug("Log is here    :  ${logFile.fullPath()}")
        workingDirectory?.let {
            logger.debug("Working dir    :  $workingDirectory")
        }

        val commandLine = scriptFile.commandLine(args)
        logger.debug("Command line   :  ${commandLine.joinToString(separator = " ")}")
        val exitCode = executeProcess(commandLine)
        if (exitCode != 0) throw Exception("Bash script execution reports failure with exit code $exitCode")
    }.onFailure {
        logger.warn("Error executing script ${it}")
    }.getOrThrow().also {
        logFile.close()
    }


    private fun executeProcess(commandLine: List<String>): Int {
        var processBuilder = ProcessBuilder(commandLine).inheritIO().redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.forAppending())).redirectErrorStream(true)
        workingDirectory?.let {
            processBuilder = processBuilder.directory(workingDirectory.toFile())
        }
        val process = processBuilder.start()
        logFile.consumeLines()
        if (background) return 0
        val exitCode = process.waitFor()
        if (process.isAlive) process.destroy()
        return exitCode
    }
}

data class Shebang(val directive: String) : ScriptRendering {
    override fun render(): String = "#!${directive}"
}

data class EnvironmentVariable(val name: String, val value: String) : ScriptRendering {
    override fun render(): String = """
        export $name="$value"
    """.trimIndent()
}

data class Body(val contents: String) : ScriptRendering {
    override fun render(): String = contents

    companion object {
        fun fromResource(resourcePath: String) =
            Body(contents = Body::class.java.getResource(resourcePath)?.readText() ?: throw IllegalArgumentException("Resource path $resourcePath could not be read"))
    }
}

data class ScriptFileGenerator(val prefix: String, val suffix: String = ".sh") : FileGenerator {
    override fun generate(): File = kotlin.io.path.createTempFile(prefix = prefix, suffix = suffix).toFile().apply {
        this.touch()
        this.makeExecutable()
    }
}

data class LogFileGenerator(val prefix: String, val suffix: String = ".log") : FileGenerator {
    override fun generate(): File = kotlin.io.path.createTempFile(prefix = prefix, suffix = suffix).toFile().apply {
        this.touch()
    }
}

data class BashScript(
    val commandName: String = "script",
    val shebang: Shebang = Shebang(directive = "/bin/bash"),
    val environmentVariables: List<EnvironmentVariable> = listOf(),
    val body: Body,
    private val scriptFileGenerator: FileGenerator? = null,
    private val logFileGenerator: FileGenerator? = null,
) : ScriptRendering {

    private val logger by lazy { LoggerFactory.getLogger(BashScript::class.java) }

    override fun render() = buildString {
        appendWithNewLine(shebang.render())
        environmentVariables.forEach { appendWithNewLine(it.render()) }
        appendWithNewLine(body.render())
    }

    private fun StringBuilder.appendWithNewLine(string: String) = this.append("$string${System.lineSeparator()}")
    fun createExecutableScript(background: Boolean = false, workingDirectory: Directory? = null): ExecutableScript =
        ExecutableScript(scriptFile = generateScriptFile(), logFile = generateLogFile(), background = background, workingDirectory = workingDirectory)

    private fun generateScriptFile(): ScriptFile {
        logger.debug("scriptFileGenerator = ${scriptFileGenerator}")
        val fileGenerator = scriptFileGenerator ?: ScriptFileGenerator(prefix = commandName)
        logger.debug("fileGenerator       = ${fileGenerator}")
        val file = fileGenerator.generate().apply { writeText(render()) }
        logger.debug("file                = ${file}")
        return ScriptFile(file = file)
    }
    private fun generateLogFile(): LogFile = LogFile((logFileGenerator ?: LogFileGenerator(prefix = commandName)).generate())

}
