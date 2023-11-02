package com.kohls.pws

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds
import com.kohls.pws.Source.*

class DslSpecification : StringSpec({

    afterTest {
        killPattern("exec:java")
    }
    "verify structure" {
        workspace {

            project("OLM Shared Utilities") {
                source = git(url = "git@gitlab.com:kohls/scps/scf/olm/olm-shared-utils.git", branch = "main", directory = "/tmp/workspace/olm-shared-utils")
            }

            project("OLM Configuration Service") {
                source = local("~/projects/olm-meta-repo/config-server")
                executesAfter("OLM Shared Utilities")
            }

            project("OLM Stub Server") {
                source = local("~/projects/olm-meta-repo/olm-stubs")
                task<MavenCommand> {
                    background = true
                    declaredVariables += "args" to "-U clean install exec:java"
                    declaredVariables += "runDirectory" to "/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs"
                    environmentVariables += "HTTPS_PROXY" to "http://proxy.kohls.com:3128"
                    validations += log(duration = 10.seconds) {
                        contains("INFO: Started Stub Server with port 8080")
                    }
                }
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
            mutableSetOf(
                Project(
                    "OLM Shared Utilities",
                    source = Source.Git(url = "git@gitlab.com:kohls/scps/scf/olm/olm-shared-utils.git", branch = "main", directory = Directory("/tmp/workspace/olm-shared-utils")),
                ),
                Project(
                    "OLM Configuration Service",
                    executesAfterList = listOf("OLM Shared Utilities"),
                    source = Source.Local(Directory("~/projects/olm-meta-repo/config-server")),
                ),
                Project(
                    name = "OLM Stub Server", source = Source.Local(Directory("~/projects/olm-meta-repo/olm-stubs")), tasks = listOf(
                        MavenCommand(
                            background = true, declaredVariables = mapOf(
                                "args" to "-U clean install exec:java", "runDirectory" to "/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs"
                            ), environmentVariables = mapOf("HTTPS_PROXY" to "http://proxy.kohls.com:3128"), validations = listOf(
                                LogValidator(
                                    duration = 10.seconds, contains = listOf("INFO: Started Stub Server with port 8080")
                                )
                            )
                        )
                    )

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
            )
        )
    }
})


fun workspace(block: WorkspaceBuilder.() -> Unit): Workspace {
    val builder = WorkspaceBuilder()
    builder.block()
    return builder.build().also {
        println("Execution Order would be:")
        val createRunbook = it.createRunbook()
        createRunbook.executionOrder.forEach { step -> println(step) }
        createRunbook.execute()

    }
}
fun WorkspaceBuilder.project(name: String, block: ProjectBuilder.() -> Unit) {
    val builder = ProjectBuilder()
    builder.block()
    builder.name = name
    projects += builder.build()
}

fun local(path: String): Source = Local(Directory(path))
fun git(url: String, branch: String, directory: String): Source = Git(url = url, branch = branch, directory = Directory(directory))

fun log(duration: kotlin.time.Duration, block: LogValidator.Builder.() -> Unit): LogValidator {
    val builder = LogValidator.Builder()
    builder.block()
    builder.duration = duration
    return builder.build()
}