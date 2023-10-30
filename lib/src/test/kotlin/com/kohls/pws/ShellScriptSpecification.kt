package com.kohls.pws

import com.kohls.pws.BashScript.Companion.from
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.time.Duration.Companion.seconds

// TODO Validation of command presence
// TODO Validation of validations

class MavenCommandSpecification : StringSpec({
    afterTest {
        killPattern("exec:java")
    }

    "execute command successfully" {
        val cmd = MavenCommand(
            background = true, declaredVariables = mapOf(
                "args" to "-U clean install exec:java", "runDirectory" to "/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs"
            ), environmentVariables = mapOf("HTTPS_PROXY" to "http://proxy.kohls.com:3128"), validations = listOf(
                Validation.Log(
                    duration = 10.seconds, contains = listOf("INFO: Started Stub Server with port 8080")
                )
            )
        )
        cmd.initialize()
        println(cmd.bashScript.contents())
        cmd.perform() shouldBe true
    }

})

class BashScriptSpecification : StringSpec({

    val bashScript = from(
        lines = listOf("echo hello \${1} \$HTTPS_PROXY", "exit 1"), environmentVariables = mapOf("HTTPS_PROXY" to "http://proxy.kohls.com:3128"),
    )
    "check that bash script is well formed" {
        bashScript.contents() shouldBe """
            #!/bin/bash
            export HTTPS_PROXY="http://proxy.kohls.com:3128"
            echo hello ${'$'}{1} ${'$'}HTTPS_PROXY
            exit 1
        """.trimIndent()
    }

    "check that bash script exits with code 1 and output is what is expected" {
        val bs = from(lines = listOf("echo hello \${1} \$HTTPS_PROXY", "exit 1"), environmentVariables = mapOf("HTTPS_PROXY" to "http://proxy.kohls.com:3128"))
        bs.execute(listOf("Chris")) shouldBe 1
        bs.logContents() shouldContainExactly listOf("hello Chris http://proxy.kohls.com:3128")
    }

    "check that bash script exits with code 0 and output is what is expected" {
        val bs = from(lines = listOf("echo hello \${1} \$HTTPS_PROXY", "exit 0"), environmentVariables = mapOf("HTTPS_PROXY" to "http://proxy.kohls.com:3128"))
        bs.execute(listOf("Chris")) shouldBe 0
        bs.logContents() shouldContainExactly listOf("hello Chris http://proxy.kohls.com:3128")
    }

})

class LogValidationSpecification : StringSpec({

    "log contains something I need" {
        Validation.Log(
            duration = 5.seconds, contains = listOf(
                "[INFO] -------------------------< com.olm:olm-stubs >--------------------------", "[INFO] --- clean:3.2.0:clean (default-clean) @ olm-stubs ---"
            )
        ).validate(
            args = mapOf(
                "abc" to "def", "logFile" to File("/tmp/olm-stubs.log")
            )
        ) shouldBe true
    }
})


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

data class BashScript(
    private val contents: String,
    private val background: Boolean,
    private val environmentVariables: Map<String, String>,
    private val declaredVariables: Map<String, String>,
    private val consumer: (String) -> Unit = CONSUMER
) {
    private val logger = LoggerFactory.getLogger(BashScript::class.java)
    private val file: File = createExecutableFile()
    private val log: Log = Log(path = "/tmp/logs/script-${System.currentTimeMillis()}.log", consumer = consumer)

    fun contents(): String = file.readText()

    // TODO Prefix name for createTempFile?
    // TODO How do I clean this up at the end of the day?
    private fun createExecutableFile(): File {
        val renderedContents = mutableListOf("#!/bin/bash")
        val file = kotlin.io.path.createTempFile().toFile().apply {
            writeText(renderContents())
            makeExecutable()
        }
        return file
    }

    private fun renderContents(): String {
        logger.debug("environment vars are $environmentVariables")
        val renderedContents = mutableListOf("#!/bin/bash")
        renderedContents += renderEnvironmentVariables()
        renderedContents += renderDeclaredVariables()
        renderedContents += contents
        val joinToString = renderedContents.joinToString("\n")
        logger.debug(joinToString)
        return joinToString
    }

    private fun renderEnvironmentVariables(): List<String> = environmentVariables.map {
        """
        export ${it.key}="${it.value}"
    """.trimIndent()
    }

    private fun renderDeclaredVariables(): List<String> = declaredVariables.map {
        """
        declare ${it.key}="${it.value}"
    """.trimIndent()
    }

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

/**
 *                     args = "-U clean install exec:java --settings ~/data/repo/maven/settings.xml"
 *
 */
data class MavenCommand(
    var background: Boolean = false,
    var declaredVariables: Map<String, String> = mutableMapOf(),
    var environmentVariables: Map<String, String> = mutableMapOf(),
    override var validations: List<Validation> = mutableListOf()
) : Task {
    private val logger: org.slf4j.Logger = LoggerFactory.getLogger(MavenCommand::class.java)
    lateinit var bashScript: BashScript

    override fun initialize() {
        this.bashScript = from(
            resourcePath = "/bash-scripts/maven-command.sh", background = true, environmentVariables = environmentVariables, declaredVariables = declaredVariables
        )
    }


    override fun perform(): Boolean {

        val exitCode = bashScript.execute()
        logger.info("exit code is $exitCode")
        if (exitCode != 0) return false
        val args = mapOf(
            "abc" to "def", "logFile" to bashScript.logFile()
        )
        return validations.map { it.validate(args) }.all { it }
    }

}


