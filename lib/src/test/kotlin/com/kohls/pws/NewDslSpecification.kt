package com.kohls.pws

import com.kohls.pws.tasks.Maven
import com.kohls.pws.tasks.MavenBuilder
import com.kohls.pws.validations.LogValidator
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.time.Duration.Companion.seconds

data class CountingGenerator(val prefix : String, var counter : Int = 0) : IdGenerator {
    override fun generate(): String = "$prefix-${++counter}"
}

// TODO:  Need to perform things in phases:
// phase 1:  compose
// phase 2:  compile
// phase 3:  confirm
class NewDslSpecification : StringSpec({
    afterTest {
        killPatterns("exec:java")
    }
    val workspaceIdGenerator = CountingGenerator("workspace")
    val projectIdGenerator =  CountingGenerator("project")
    val taskIdGenerator =  CountingGenerator("task")

    val workspace = workspace(workspaceIdGenerator) {
//        project("OLM Shared Utilities") {
//            gitSource(url = "git@gitlab.com:kohls/scps/scf/olm/olm-shared-utils.git", branch = "main", directory = "/tmp/workspace/olm-shared-utils")
//        }
//        project("OLM Configuration Service") {
//            localSource(path = "~/projects/olm-meta-repo/config-server")
//            dependencies {
//                executesAfter("OLM Shared Utilities")
//            }
//        }
        project("OLM Stub Server", projectIdGenerator = projectIdGenerator) {
            localSource(path = "/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs")
            task<MavenBuilder>(idGenerator = taskIdGenerator) {
                maven(background = true) {
                    args("-U", "clean", "install", "exec:java")
                    proxy("http://proxy.kohls.com:3128")
                    settings("/Users/TKMA5QX/data/repo/maven/settings.xml")
                    pom("/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs/pom.xml")
                }
                validations {
                    logContains(duration = 10.seconds, "INFO: Started Stub Server with port 8080")
                }
            }
        }
//        project("OLM Gateway Service", parallel = true) {
//            gitSource(url = "git@gitlab.com:kohls/scps/scf/olm/olm-gateway.git", branch = "main", directory = "/tmp/workspace/olm-gateway")
//            dependencies {
//                executesAfter("OLM Shared Utilities")
//                executesAfter("OLM Configuration Service")
//            }
//        }
//        project("OLM Order Create Service", parallel = true) {
//            gitSource(url = "git@gitlab.com:kohls/scps/scf/olm/order-create.git", branch = "main", directory = "/tmp/workspace/order-create")
//            dependencies {
//                executesAfter("OLM Shared Utilities")
//                executesAfter("OLM Configuration Service")
//            }
//        }
    }


    "DSL creates a Workspace with correct projects and configurations" {

        println("workspace = ${workspace}")
        workspace.projects shouldHaveSize 1

        workspace shouldBe Workspace(
            id = "workspace-1",
            projects = listOf(
////                Project(
////                    name = "OLM Shared Utilities", source = GitSource(url = "git@gitlab.com:kohls/scps/scf/olm/olm-shared-utils.git", branch = "main", directory = "/tmp/workspace/olm-shared-utils")
////                ),
//                Project(
//                    name = "OLM Configuration Service", source = LocalSource(path = "~/projects/olm-meta-repo/config-server"), dependencies = listOf("OLM Shared Utilities")
//                ),
                Project(
                    id = "project-1",
                    name = "OLM Stub Server",
                    source = LocalSource(path = "/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs"),
                    tasks = listOf(
                        Maven(
                            id = "task-1",
                            background = true,
                            pomXmlFilePath = File("/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs/pom.xml"),
                            settingsXmlFilePath = File("/Users/TKMA5QX/data/repo/maven/settings.xml"),
                            variables = mutableMapOf("HTTPS_PROXY" to "http://proxy.kohls.com:3128"),
                            args = listOf("-U", "clean", "install", "exec:java"),
                            validations = listOf(LogValidator(duration = 10.seconds, contains = listOf("INFO: Started Stub Server with port 8080"))),
                        )
                    ),
                ),
//                Project(
//                    name = "OLM Gateway Service",
//                    source = GitSource(url = "git@gitlab.com:kohls/scps/scf/olm/olm-gateway.git", branch = "main", directory = "/tmp/workspace/olm-gateway"),
//                    dependencies = listOf("OLM Shared Utilities", "OLM Configuration Service"),
//                    parallel = true,
//                ),
//                Project(
//                    name = "OLM Order Create Service",
//                    source = GitSource(url = "git@gitlab.com:kohls/scps/scf/olm/order-create.git", branch = "main", directory = "/tmp/workspace/order-create"),
//                    dependencies = listOf("OLM Shared Utilities", "OLM Configuration Service"),
//                    parallel = true,
//                ),
            )
        )
    }

    "run workspace" {
        workspace.execute()
    }
})

fun workspace(workspaceIdGenerator: IdGenerator = IdGenerator.Universal("workspace"), block: WorkspaceBuilder.() -> Unit): Workspace {
    val workspace = WorkspaceBuilder(workspaceIdGenerator).apply(block).build()
    val lookupTable = LookupTable(workspace)

    return workspace.compile(lookupTable = lookupTable)
}


