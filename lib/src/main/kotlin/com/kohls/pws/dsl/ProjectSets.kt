package com.kohls.pws.dsl

import com.kohls.pws.Parameters
import com.kohls.pws.model.Project
import com.kohls.pws.model.ProjectSet
import com.kohls.pws.model.ProjectSetName

class ProjectSetBuilder(val name: String) {
    val projects = mutableSetOf<Project>()
    fun build(): ProjectSet {
        val projectSetName = ProjectSetName(name)
        val parameters = Parameters.create("projectSetName" to projectSetName)
        return ProjectSet(projectSetName, projects, parameters)
    }

    fun project(name: String, block: ProjectBuilder.() -> Unit) {
        val builder = ProjectBuilder(name)
        projects += builder.apply(block).build()
    }
}

fun projectSet(name: String, block: ProjectSetBuilder.() -> Unit): ProjectSet {
    val builder = ProjectSetBuilder(name)
    return builder.apply(block).build()
}