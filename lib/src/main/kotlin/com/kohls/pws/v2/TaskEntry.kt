package com.kohls.pws.v2

data class TaskEntry(val project: Project) {
    fun getProjectSourcePath(): String {
        return project.getSourcePath()
    }
}