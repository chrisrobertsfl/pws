package com.kohls.pws

import com.kohls.base.DependencyGraph
import com.kohls.pws.tasks.ConfirmationException

fun workspace(workspaceIdGenerator: IdGenerator = IdGenerator.Universal("workspace"), block: WorkspaceBuilder.() -> Unit): Workspace {
    val workspace = WorkspaceBuilder(workspaceIdGenerator).apply(block).build()
    val lookupTable = LookupTable(workspace)
    return workspace.compile(lookupTable = lookupTable)
}


data class Workspace(override val id: String, var projects: List<Project>) : Entity<Workspace> {
    fun execute() {
        createRunbook().execute()
    }

    fun createRunbook(): Runbook {
        val projectMap = projects.associateBy { it.name }
        val executionOrder = mutableListOf<Step>()
        val visited = mutableSetOf<Project>()

        fun visit(project: Project) {
            if (project in visited) return
            project.dependencies.forEach { dependencyName ->  // changed from 'dependency' to 'dependencyName'
                val dependentProject = projectMap[dependencyName]  // changed from 'dependency' to 'dependencyName'
                if (dependentProject != null) visit(dependentProject)
            }
            visited.add(project)
            executionOrder.add(Step(project))
        }

        projects.forEach { project -> visit(project) }

        return Runbook(executionOrder)
    }

    override fun compile(lookupTable: LookupTable): Workspace = apply { projects = projects.map { it.compile(lookupTable) } }

    override fun confirm(): Workspace {
        val graph = createGraphOfDependencies()
        val orphanedDependencies = detectOrphanedDependencies(graph)
        val missingProjectNames = formatMissingProjectNames(orphanedDependencies)
        missingProjectNames?.let {
            throw ConfirmationException(
                message = "Confirmation failed for $id", errors = listOf(ConfirmationException.Error(text = "Missing projects: names $it"))
            )
        }
        val cycle = graph.findCycle()
        val circularReferencingProjectNames = cycle.takeIf { it.isNotEmpty() }?.sorted()?.joinToString(prefix = "[", separator = ", ", postfix = "]") { "'$it'" }
        circularReferencingProjectNames?.let {
            throw ConfirmationException(
                message = "Confirmation failed for $id", errors = listOf(ConfirmationException.Error(text = "Circular referencing projects: names $it"))
            )
        }
        val duplicateProjects = findDuplicateProjectNames()
        val duplicateProjectNames = duplicateProjects.takeIf { it.isNotEmpty() }?.sorted()?.joinToString(prefix = "[", separator = ", ", postfix = "]") { "'$it'" }
        duplicateProjectNames?.let {
            throw ConfirmationException(
                message = "Confirmation failed for $id", errors = listOf(ConfirmationException.Error(text = "Duplicate projects: names $it"))
            )
        }
        return this
    }
    private fun findDuplicateProjectNames(): Set<String> = projects.groupingBy { it.name }.eachCount().filter { it.value > 1 }.keys


    private fun formatMissingProjectNames(orphanedDependencies: Set<String>) =
        orphanedDependencies.takeIf { it.isNotEmpty() }?.sorted()?.joinToString(prefix = "[", separator = ", ", postfix = "]") { "'${it}'" }

    private fun detectOrphanedDependencies(graph: DependencyGraph) = graph.findSinks()
    private fun createGraphOfDependencies() = DependencyGraph(adjacencyMap = createAdjacencyMapFromProjects())
    private fun createAdjacencyMapFromProjects() = projects.associate { it.name to it.dependencies }
}


class WorkspaceBuilder(private val idGenerator: IdGenerator = IdGenerator.Universal("workspace")) {
    val projectBuilders: MutableList<ProjectBuilder> = mutableListOf()
    fun build() = Workspace(id = idGenerator.generate(), projects = projectBuilders.map { it.build() })

    fun project(name: String, parallel: Boolean = false, projectIdGenerator: IdGenerator, block: ProjectBuilder.() -> Unit) {
        projectBuilders += ProjectBuilder(projectIdGenerator).apply {
            this.name = name
            this.parallel = parallel
            apply(block)
        }
    }
}


