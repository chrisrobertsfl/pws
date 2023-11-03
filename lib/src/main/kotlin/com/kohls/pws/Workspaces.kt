package com.kohls.pws

import com.ingenifi.engine.ClasspathResource
import com.kohls.pws.tasks.ConfirmationException

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
        val errors = ConfirmationEngine(ruleResources = listOf(ClasspathResource("rules/workspace.drl"))).run<Workspace, ConfirmationException.Error>(this)
        if (errors.isNotEmpty()) throw ConfirmationException(message = "Confirmation failed for workspace $id", errors = errors)
        return this
    }
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


