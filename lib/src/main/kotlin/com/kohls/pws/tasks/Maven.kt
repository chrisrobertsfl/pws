package com.kohls.pws.tasks

import com.ingenifi.engine.ClasspathResource
import com.ingenifi.engine.Engine
import com.ingenifi.engine.Option.*
import com.kohls.pws.*
import com.kohls.pws.tasks.ConfirmationException.Error
import org.slf4j.LoggerFactory.getLogger
import java.io.File

data class Maven(
    override val id: String,
    val args: List<String>,
    val variables: MutableMap<String, String>,
    val background: Boolean,
    var settingsXmlFilePath: File?,
    var pomXmlFilePath: File?,
    override val validations: List<Validation>
) : Task {
    private val logger = getLogger(Maven::class.java)

    lateinit var bashScript: BashScript
    override fun initialize() {
        bashScript = BashScriptFactory.fromResource(resourcePath = "/bash-scripts/new-maven-command.sh", background = background, variables = variables)
    }

    override fun perform(): Boolean {
        val exitCode = bashScript.execute(args)
        logger.info("exit code is $exitCode")
        if (exitCode != 0) return false
        val validationArguments = mapOf("logFile" to bashScript.logFile())
        return validations.map { it.validate(validationArguments) }.all { it }
    }

    override fun compile(lookupTable: LookupTable): Task {
        val entry = lookupTable.using(this)
        val projectSourcePath = entry.getProjectSourcePath()
        return apply {
            pomXmlFilePath = pomXmlFilePath ?: File("$projectSourcePath/pom.xml")
            settingsXmlFilePath = settingsXmlFilePath ?: File("$projectSourcePath/settings.xml")
        }
    }

    override fun confirm(): Task {
        val errors = ConfirmationEngine(ruleResources = listOf(ClasspathResource("rules/task.drl")))
            .run<Task, Error>(this)
        if (errors.isNotEmpty()) throw ConfirmationException(message = "Confirmation failed for task $id", errors = errors)
        return this
    }
}

class ConfirmationException(val errors: List<Error>, message: String) : Exception(message) {
    data class Error(val text: String)
}
