package com.kohls.pws.model

import com.kohls.pws.Parameters

data class ProjectSet(val name: ProjectSetName, val projects: Set<Project>, val parameters : Parameters) {
    fun execute()  {
        println("Running Project Set '${name.contents}'")
         var input = Parameters.EMPTY
        for (project in projects) {
            println("Running Project '${project.name.contents}'")
            for (action in project.actions) {
                println("Running Action '${action.name.contents}'")
                input = action.perform(input)
            }
        }
    }
}