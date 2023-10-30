package com.kohls.pws

class WorkspaceBuilder {
    private lateinit var targetDirectory: Directory
    val projects: MutableSet<Project> = mutableSetOf()
    fun build(): Workspace = Workspace(targetDirectory = targetDirectory, projects = projects)

    fun targetDirectory(path: String) {
        this.targetDirectory = Directory(path)
    }
}