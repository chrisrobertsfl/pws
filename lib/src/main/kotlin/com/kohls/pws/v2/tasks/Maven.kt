package com.kohls.pws.v2.tasks

import com.kohls.pws.MavenCommand
import com.kohls.pws.v2.BashScript
import com.kohls.pws.v2.BashScriptFactory
import com.kohls.pws.v2.Task
import com.kohls.pws.v2.Validation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

data class Maven(
    val args: List<String>, val variables: MutableMap<String, String>, val background: Boolean, var settingsXmlFilePath: File?, var pomXmlFilePath: File?, override val validations: List<Validation>
) : Task {
    private val logger: Logger = LoggerFactory.getLogger(MavenCommand::class.java)

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
}