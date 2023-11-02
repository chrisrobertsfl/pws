package com.kohls.pws

data class TaskEntry(val project: Project) {
    fun getProjectSourcePath(): String {
        return project.getSourcePath()
    }
}