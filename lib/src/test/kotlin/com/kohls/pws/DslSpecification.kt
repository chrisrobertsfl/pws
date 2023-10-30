package com.kohls.pws

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File

class DslSpecification : StringSpec({
    "verify structure" {
        workspace("/tmp/hello") {

            project("OLM Shared Utilities") {
                source = git(url = "git@gitlab.com:kohls/scps/scf/olm/olm-shared-utils.git", branch = "main", directory = "/tmp/workspace/olm-shared-utils")
            }

            project("OLM Configuration Service") {
                source = local("~/projects/olm-meta-repo/config-server")
                executesAfter("OLM Shared Utilities")
            }

            project("OLM Stub Server") {
                source = local("~/projects/olm-meta-repo/olm-stubs")
            }
            project("OLM Gateway Service") {
                source = git(url = "git@gitlab.com:kohls/scps/scf/olm/olm-gateway.git", branch = "main", directory = "/tmp/workspace/olm-gateway")
                canBeParallelized = true
                executesAfter("OLM Shared Utilities")
                executesAfter("OLM Configuration Service")
            }

            project("OLM Order Create Service") {
                source = git(url = "git@gitlab.com:kohls/scps/scf/olm/order-create.git", branch = "main", directory = "/tmp/workspace/order-create")
                canBeParallelized = true
                executesAfter("OLM Shared Utilities")
                executesAfter("OLM Configuration Service")
            }

        } shouldBe Workspace(
            Directory("/tmp/hello"), mutableSetOf(
                Project(
                    "OLM Configuration Service",
                    executesAfterList = listOf("OLM Shared Utilities"),
                    source = Source.Local(Directory("~/projects/olm-meta-repo/config-server")),
                ),
                Project(
                    "OLM Stub Server", source = Source.Local(Directory("~/projects/olm-meta-repo/olm-stubs"))
                ),
                Project(
                    "OLM Gateway Service",
                    executesAfterList = listOf("OLM Shared Utilities", "OLM Configuration Service"),
                    source = Source.Git(url = "git@gitlab.com:kohls/scps/scf/olm/olm-gateway.git", branch = "main", directory = Directory("/tmp/workspace/olm-gateway")),
                    canBeParallelized = true
                ),
                Project(
                    "OLM Order Create Service",
                    executesAfterList = listOf("OLM Shared Utilities", "OLM Configuration Service"),
                    source = Source.Git(url = "git@gitlab.com:kohls/scps/scf/olm/order-create.git", branch = "main", directory = Directory("/tmp/workspace/order-create")),
                    canBeParallelized = true
                ),
                Project(
                    "OLM Shared Utilities",
                    source = Source.Git(url = "git@gitlab.com:kohls/scps/scf/olm/olm-shared-utils.git", branch = "main", directory = Directory("/tmp/workspace/olm-shared-utils")),
                ),
            )
        )
    }
})


fun workspace(targetDirectory: String, block: Workspace.Builder.() -> Unit): Workspace {
    val builder = Workspace.Builder()
    builder.block()
    builder.targetDirectory(targetDirectory)
    return builder.build().also {
        println("Execution Order would be:")
        val createRunbook = it.createRunbook()
        createRunbook.executionOrder.forEach { step -> println(step) }
        createRunbook.execute()

    }
}

fun Workspace.Builder.project(name: String, block: Project.Builder.() -> Unit) {
    val builder = Project.Builder()
    builder.block()
    builder.name = name
    projects += builder.build()
}

data class Workspace(val targetDirectory: Directory, val projects: Set<Project>) {

    fun createRunbook(): Runbook {
        val projectMap = projects.associateBy { it.name }
        val executionOrder = mutableListOf<Step>()
        val visited = mutableSetOf<Project>()

        fun visit(project: Project) {
            if (project in visited) return
            project.executesAfterList.forEach { dependency ->
                val dependentProject = projectMap[dependency]
                if (dependentProject != null) {
                    visit(dependentProject)
                }
            }
            visited.add(project)
            executionOrder.add(Step(project))
        }

        projects.forEach { project ->
            visit(project)
        }

        return Runbook(executionOrder)
    }

    class Builder {
        private lateinit var targetDirectory: Directory
        val projects: MutableSet<Project> = mutableSetOf()
        fun build(): Workspace {
            return Workspace(targetDirectory = targetDirectory, projects = projects)
        }

        fun targetDirectory(path: String) {
            this.targetDirectory = Directory(path)
        }
    }
}

data class Project(val name: String, val executesAfterList: List<String> = listOf(), val source: Source = Source.UNKNOWN, val canBeParallelized: Boolean = false) {
    class Builder {
        lateinit var name: String
        private var executesAfterList: List<String> = mutableListOf()
        var source: Source = Source.UNKNOWN
        var canBeParallelized: Boolean = false
        fun build(): Project {
            return Project(name = name, executesAfterList = executesAfterList, source = source, canBeParallelized = canBeParallelized)
        }

        fun executesAfter(name: String) {
            executesAfterList += name
        }

        fun local(path: String): Source = Source.Local(Directory(path))
        fun git(url: String, branch: String, directory: String): Source = Source.Git(url = url, branch = branch, directory = Directory(directory))
    }
}

sealed interface Source {
    object UNKNOWN : Source
    data class Local(val directory: Directory) : Source
    data class Git(val url: String, val branch: String, val directory: Directory) : Source
}

data class Directory(val path: String) {
    private val directoryPath: File = File(path)
    fun delete() = directoryPath.deleteRecursively()
    fun create() = directoryPath.mkdirs()
    private fun exists(): Boolean = directoryPath.exists()
    fun canBeCreated() = !exists() && directoryPath.parentFile?.canWrite() ?: false
    fun isNotThere(): Boolean = path.isBlank()
}

data class Runbook(val executionOrder: List<Step>) {
    fun execute() {
        runBlocking {
            val deferredSteps = mutableListOf<Deferred<Unit>>()
            val executedSteps = mutableSetOf<Step>()

            for (step in executionOrder) {
                val dependencies = step.project.executesAfterList.mapNotNull { dependencyName ->
                    executionOrder.find { it.project.name == dependencyName }
                }

                if (step.project.canBeParallelized || dependencies.all { it in executedSteps }) {
                    val deferred = async {
                        // Execute the project here based on its properties
                        println("Executing ${step.project.name}...")
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


data class Step(val project: Project)


