package com.kohls.pws

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID.randomUUID

fun workspace(name: String, block: WorkspaceConfig.() -> Unit = {}): Workspace {
    return WorkspaceConfig(name).apply(block).configure()
}

data class Workspace(val name: String = randomUUID().toString(), val projects: List<Project> = emptyList(), var logger: Logger = LoggerFactory.getLogger(Workspace::class.java)) {
    fun execute() = try {
        for (project in projects) {
            logger.info("Executing Project : ${project.name}")
            var parameters: Parameters = Parameters.create()
            for (action in project.actions) {
                logger.info("Performing Action: ${action.name}")
                parameters = action.perform(parameters)
            }
        }
    } catch (exception: Exception) {
        logger.error(exception.message)
    }
}

data class WorkspaceConfig(val name: String) {
    private val projects = mutableListOf<Project>()
    fun configure() = Workspace(name, projects)

    fun project(name: String, block: ProjectConfig.() -> Unit = {}) {
        projects += ProjectConfig(name).apply(block).configure()
    }
}