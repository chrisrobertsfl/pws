package com.kohls.pws.v2

import com.kohls.pws.v2.tasks.Maven
import java.io.File

interface WorkspaceCompiler {
    fun compile(workspace: Workspace): Workspace

    object None : WorkspaceCompiler {
        override fun compile(workspace: Workspace): Workspace = workspace
    }

    class Standard : WorkspaceCompiler {
        override fun compile(workspace: Workspace): Workspace {
            // TODO: Unify this somehow in source
            workspace.projects.forEach { project ->
                val path = project.getSourcePath()
                val pomXmlFilePath = File("$path/pom.xml")
                val settingsXmlFilePath = File("${System.getProperty("user.home")}/.m2/settings.xml")
                project.tasks.filter { it is Maven && it.pomXmlFilePath == null }.map { it as Maven }.forEach { it.pomXmlFilePath = pomXmlFilePath }
                project.tasks.filter { it is Maven && it.settingsXmlFilePath == null }.map { it as Maven }.forEach { it.settingsXmlFilePath = settingsXmlFilePath }
            }
            return workspace
        }

    }
}