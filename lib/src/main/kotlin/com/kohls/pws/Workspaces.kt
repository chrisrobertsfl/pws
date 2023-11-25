package com.kohls.pws

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID.randomUUID

fun workspace(name: String, block: WorkspaceConfig.() -> Unit = {}): Workspace {
    val configure = WorkspaceConfig(name).apply(block).configure()
    return configure
}

data class Workspace(val name: String = randomUUID().toString(), val projects: List<Project> = emptyList(), var parameters: Parameters = Parameters.create(), var logger: Logger = LoggerFactory.getLogger(Workspace::class.java)) {
    fun execute() = try {
        for (project in projects) {
            var projectParameters = parameters.copy().also {
                it += "projectName" to project.name
            }
            logger.info("Executing Project : ${project.name}")
            for (action in project.actions) {
                logger.info("Performing Action : ${action.name}")
                projectParameters = action.perform(projectParameters)
            }
        }
    } catch (exception: Exception) {
        logger.error(exception.message)
    }
}

data class WorkspaceConfig(val name: String) {
    private val projects = mutableListOf<Project>()
    private var parameters  = Parameters()
    fun configure() = Workspace(name, projects, parameters = parameters)

    fun project(name: String, block: ProjectConfig.() -> Unit = {}) {
        projects += ProjectConfig(name).apply(block).configure()
    }

    fun targetParentPath(targetParentPath : String) {
        parameters += "targetParentPath" to targetParentPath
    }
    fun settingsXmlFilePath(settingsXmlFilePath : String) {
        parameters += "settingsXmlFilePath" to settingsXmlFilePath
    }
}