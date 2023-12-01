package com.kohls.pws.model

import com.kohls.base.Directory
import com.kohls.pws.BashScript
import com.kohls.pws.Body
import com.kohls.pws.Parameters

data class MavenAction(override val name: ActionName, private var target : Directory?, private var pom : XmlFile?, val settings : XmlFile, val goals : MavenGoals, val bashScript: BashScript = BASH_SCRIPT) : Action {
    override fun perform(): String {
        TODO("Not yet implemented")
    }

    override fun perform(input: Parameters): Parameters {
        val workingDirectory = target ?: input.getOrThrow<Directory>("target")
        val foundPom = pom ?: XmlFile("${workingDirectory.path}/pom.xml")
        val executableScript = bashScript.createExecutableScript(background = true, workingDirectory = workingDirectory)
        val args: List<Any> = listOf(foundPom.path, settings.path) + goals
        executableScript.execute(args)
        return Parameters.create("logFile" to executableScript.logFile)
    }

    companion object {
        val BASH_SCRIPT = BashScript(commandName = "maven", body = Body.fromResource("/bash-scripts/bash-maven.sh"))
    }
}

data class XmlFile(val path : String)
data class MavenGoals(val goals : List<String> )