package com.kohls.pws.v2

data class Workspace(val id : String , val projects: Set<Project>) {
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

    fun compile(compiler: WorkspaceCompiler = WorkspaceCompiler.Standard()): Workspace = compiler.compile(this)

}


class WorkspaceBuilder(private val idGenerator : IdGenerator = IdGenerator.Universal("workspace")) {
    val projectBuilders: MutableSet<ProjectBuilder> = mutableSetOf()
    fun build(): Workspace {
        val builtProjects = projectBuilders.map { it.build() }.toSet()
        return Workspace(id = idGenerator.generate(), projects = builtProjects)
    }

    fun project(name: String, parallel: Boolean = false, projectIdGenerator : IdGenerator, block: ProjectBuilder.() -> Unit) {
        projectBuilders += ProjectBuilder(projectIdGenerator).apply {
            this.name = name
            this.parallel = parallel
            apply(block)
        }
    }
}


