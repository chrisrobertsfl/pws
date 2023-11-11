package com.kohls.pws

import java.io.File

data class Maven(override val name: String, var pomXmlFile : File? = null, var settingsXmlFile : File? = null, val goals: MutableList<String> = mutableListOf()) : Action {

    fun goals(vararg goals: String) {
        this.goals.addAll(goals)
    }

    override fun perform(): Map<String, Any> {
        val executableScript = BashScript(commandName = "maven", body = Body.fromResource("/bash-scripts/bash-maven.sh")).createExecutableScript(background = true)
        val pomXmlFilePath = requireNotNull(pomXmlFile) { "Missing pomXmlFile" }.path
        val settingsXmlFilePath = requireNotNull(settingsXmlFile) { "Missing settingsXmlFile" }.path


        val args = listOf(pomXmlFilePath , settingsXmlFilePath) + goals
        println("args = '${args}' (size = ${args.size})")
        executableScript.execute(args)
        return mapOf("logFile" to executableScript.logFile)
    }

}