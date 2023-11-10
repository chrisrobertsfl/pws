package com.kohls.pws2

import com.kohls.base.DependencyGraph
import com.kohls.base.TraversalType

fun workspace(name: String, block: WorkspaceConfig.() -> Unit = {}): Workspace {
    return WorkspaceConfig(name).apply(block).configure()
}

data class Workspace(val name: String, val projects: Map<String, Project> = emptyMap())

data class WorkspaceConfig(val name: String) {
    private val projects = mutableMapOf<String, Project>()
    private val executionPlanner: WorkPlanner = StandardExecutionPlanner("standard") // TODO:  Can be changed later on
    fun configure() = Workspace(name, projects)

    fun project(name: String, block: ProjectConfig.() -> Unit = {}) {
        projects += name to ProjectConfig(name).apply(block).configure()
    }
}

interface WorkPlanner {
    val name: String
    fun plan(workspace: Workspace): Runbook
}

data class StandardExecutionPlanner(override val name: String) : WorkPlanner {
    override fun plan(workspace: Workspace): Runbook {
        val schedule: LinkedHashMap<String, List<String>> = linkedMapOf()
        val projectNames = deriveScheduledProjectNames(workspace)
        for (projectName in projectNames) {
            schedule += projectName to deriveScheduledActionNames(workspace.projects[projectName] ?: throw IllegalArgumentException("Missing Project for name $projectName"))
        }
        return Runbook(schedule)
    }

    private fun deriveScheduledProjectNames(workspace: Workspace): List<String> {
        val adjacencyMap = workspace.projects.map { it.key to it.value.dependencies.map { dependency -> dependency.name }.toList() }.toMap()
        return deriveScheduledNames(adjacencyMap)
    }

    private fun deriveScheduledActionNames(project: Project): List<String> {
        val adjacencyMap = project.actions.map { it.key to it.value.dependencies.map { dependency -> dependency.name }.toList() }.toMap()
        return deriveScheduledNames(adjacencyMap)
    }

    private fun deriveScheduledNames(adjacencyMap: Map<String, List<String>>): List<String> {
        val graph = DependencyGraph(adjacencyMap)
        val scheduledNames: MutableList<String> = mutableListOf()
        graph.dfsVisit(graph.findMostDependentNode(), TraversalType.POST_ORDER) { scheduledNames += it }
        return if (scheduledNames.size == 1 && scheduledNames[0].isEmpty()) emptyList() else scheduledNames
    }

}

data class Runbook(val schedule: LinkedHashMap<String, List<String>> = linkedMapOf())

