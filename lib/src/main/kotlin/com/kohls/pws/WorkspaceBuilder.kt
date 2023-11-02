package com.kohls.pws

class WorkspaceBuilder() {
    val projects: MutableSet<Project> = mutableSetOf()
    fun build(): Workspace = Workspace(projects = projects)
}