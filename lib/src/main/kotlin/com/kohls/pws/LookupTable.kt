package com.kohls.pws

data class LookupTable(private val workspace: Workspace) {
    private val taskEntries: Map<String, TaskEntry>
    init {
        taskEntries = mutableMapOf()
        for (project in workspace.projects) {
          for (task in project.tasks) {
              taskEntries += task.id to  TaskEntry(project)
          }
        }
    }
    fun using(task: Task): TaskEntry = taskEntries[task.id] ?: throw IllegalArgumentException("No task id found in lookup table")
}

data class TaskEntry(val project: Project) {
    fun getProjectSourcePath(): String = project.source.path.path
}