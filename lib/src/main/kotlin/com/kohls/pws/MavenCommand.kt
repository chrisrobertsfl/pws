package com.kohls.pws

import com.kohls.pws.BashScript.Companion.from
import org.slf4j.LoggerFactory

// TODO Validation of command presence
// TODO Validation of validations


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


