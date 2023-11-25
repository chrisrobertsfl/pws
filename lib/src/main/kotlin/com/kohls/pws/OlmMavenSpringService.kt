package com.kohls.pws

import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class OlmMavenSpringService(override val name: String = generateName()) : Action {
    var repositoryName: String? = null
    var targetDirectoryPath: String? = null
    var settingsXmlFilePath: String? = null
    var branchName: String? = null
    val goals = mutableListOf<String>()
    var applicationName: String? = null
    var healthcheckPort: Int? = null

    private val logger by lazy { LoggerFactory.getLogger(OlmMavenSpringService::class.java) }

    override fun perform(parameters: Parameters): Parameters {
        logger.trace("parameters are {}", parameters)
        requireNotNull(applicationName) { "Missing applicationName" }
        requireNotNull(healthcheckPort) { "Missing healthcheckPort" }
        if (targetDirectoryPath == null) {
            val targetParentPath = parameters.getOrThrow<String>("targetParentPath")
            targetDirectoryPath = "${targetParentPath}/${repositoryName}"
        }
        if (settingsXmlFilePath == null) {
            settingsXmlFilePath = parameters.getOrThrow<String>("settingsXmlFilePath")
        }
        if (branchName == null) {
            branchName = BRANCH_NAME
        }
        if (goals.isEmpty()) {
            goals += "spring-boot:run"
        }
        var currentParameters = parameters.copy()
        currentParameters = performGitPrepare(currentParameters)
        currentParameters = performMaven(currentParameters)
        currentParameters = performLogFileCheck(currentParameters)
        currentParameters = performHealthCheck(currentParameters)
        return currentParameters
    }

    private fun performGitPrepare(parameters: Parameters): Parameters {
        require(!repositoryName.isNullOrBlank()) { "Missing repositoryName" }
        val gitPrepare = GitPrepare(name = "Git Prepare - $name").apply {
            repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/$repositoryName.git"
            targetDirectoryPath = this@OlmMavenSpringService.targetDirectoryPath
            branchName = this@OlmMavenSpringService.branchName!!
        }
        return gitPrepare.perform(parameters)
    }

    private fun performMaven(parameters: Parameters): Parameters {
        require(!repositoryName.isNullOrBlank()) { "Missing repositoryName" }
        val maven = Maven(name = "Maven - $name").apply {
            pomXmlFilePath = "${targetDirectoryPath}/pom.xml"
            settingsXmlFilePath = this@OlmMavenSpringService.settingsXmlFilePath
            workingDirectoryPath = targetDirectoryPath
            goals.addAll(this@OlmMavenSpringService.goals)
        }
        return maven.perform(parameters)
    }

    private fun performLogFileCheck(parameters: Parameters): Parameters {
        val logFileEventuallyContains = LogFileEventuallyContains(name = "LogFileEventuallyContains - $name").apply {
            initialDelay = 5.seconds
            duration = 20.minutes
            interval = 30.seconds
            searchedText = "Started $applicationName in"
        }
        return logFileEventuallyContains.perform(parameters)
    }

    private fun performHealthCheck(parameters: Parameters): Parameters {
        val jsonResponseHealthCheck = JsonResponseHealthCheck(name = "LogFileEventuallyContains - $name").apply {
            url = "http://localhost:$healthcheckPort/actuator/health"
            searchedField = "status" to "UP"
            attempts = 60
            interval = 1.seconds
        }
        return jsonResponseHealthCheck.perform(parameters)
    }

    companion object {
        private const val BRANCH_NAME: String = "main"
    }

}