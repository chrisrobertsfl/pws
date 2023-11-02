package com.kohls.pws

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

data class Step(val project: Project)



data class Runbook(val executionOrder: List<Step>) {
    fun execute() {
        runBlocking {
            val deferredSteps = mutableListOf<Deferred<Unit>>()
            val executedSteps = mutableSetOf<Step>()

            for (step in executionOrder) {
                val dependencies = step.project.dependencies.mapNotNull { dependencyName ->
                    executionOrder.find { it.project.name == dependencyName }
                }

                if (step.project.parallel || dependencies.all { it in executedSteps }) {
                    val deferred = async {
                        // Execute the project here based on its properties
                        println("Executing ${step.project.name}...")
                        step.project.tasks.forEach {
                            it.initialize()
                            it.perform()
                        }
                        // Add your logic here to execute the project
                    }
                    deferredSteps.add(deferred)
                    executedSteps.add(step)
                }
            }
            deferredSteps.forEach { it.await() }
        }
    }
}