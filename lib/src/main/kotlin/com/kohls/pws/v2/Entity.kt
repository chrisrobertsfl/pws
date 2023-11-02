package com.kohls.pws.v2


interface Entity<T> : IsCompilable<T>, HasIdentity

interface HasIdentity {
    val id: String
}

interface IsCompilable<T> {
    fun compile(lookupTable: LookupTable): T
}

data class LookupTable(private val workspace: Workspace) {
    private val taskEntries: Map<String, TaskEntry>

    init {
        taskEntries = mutableMapOf()
        workspace.projects.forEach { project ->
            project.tasks.forEach { task ->
                taskEntries[task.id] = TaskEntry(project)
            }
        }
    }

    fun using(task: Task): TaskEntry {
        return taskEntries[task.id] ?: throw IllegalArgumentException("No task id found in lookup table")
    }

}

data class TaskEntry(val project: Project) {
    fun getProjectSourcePath(): String {
        return project.getSourcePath()
    }
}