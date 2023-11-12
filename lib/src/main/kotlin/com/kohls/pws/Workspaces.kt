package com.kohls.pws

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.UUID.randomUUID

fun generateName() = randomUUID().toString()


fun workspace(name: String = generateName(), block: WorkspaceConfig.() -> Unit = {}): Workspace {
    return WorkspaceConfig(name).apply(block).configure()
}

data class Workspace(val name: String = randomUUID().toString(), val projects: List<Project> = emptyList(), val logger: Logger = LoggerFactory.getLogger(Workspace::class.java)) {
    fun execute() = try {
        for (project in projects) {
            var parameters: Parameters = Parameters.create()
            for (action in project.actions) {
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

    fun project(name: String = generateName(), block: ProjectConfig.() -> Unit = {}) {
        projects += ProjectConfig(name).apply(block).configure()
    }
}