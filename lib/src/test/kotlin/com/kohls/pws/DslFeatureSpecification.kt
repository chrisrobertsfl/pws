package com.kohls.pws

import com.kohls.base.Directory
import com.kohls.base.nonExistingDirectory
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.slf4j.Logger

class DslFeatureSpecification : FeatureSpec({
    feature("Simple workspace creation using DSL") {
        scenario("creating a simple workspace with no projects") {
            workspace("simple") shouldBe Workspace("simple")
        }

        scenario("creating a simple workspace with one project") {
            workspace("workspace") {
                project("project")
            } shouldBe Workspace("workspace", listOf(Project("project")))
        }
    }

    feature("Complex workspace creation using DSL") {

        beforeTest {
            ActionRegistry.register(Maven::class) { name -> MavenConfiguration(name) }
            ActionRegistry.register(GitClone::class) { name -> GitCloneConfig(name) }
            ActionRegistry.register(NoOp::class) { name -> NoOp(name) }
        }

        scenario("creating a complex project") {
            workspace("complex-workspace") {
                project("complex") {

                }
            } shouldBe Workspace(
                "complex-workspace", projects = listOf(
                    Project(
                        "complex", listOf(Maven("complex build", goals = mutableListOf("clean")), NoOp("another"))
                    )
                )
            )
        }
    }

    feature("When something goes wrong, during execution") {
        scenario("Directory for action does not exist") {
            val action = ValidDirectoryExists(directory = nonExistingDirectory("/default/project"))
            val project = Project(actions = listOf(action))
            val workspace = Workspace(projects = listOf(project), logger = mock(Logger::class.java))
            workspace.execute()
            verify(workspace.logger).error("Invalid directory /default/project")
        }
    }

    feature("Olm Stubs clone, run, and healthcheck") {

        beforeTest {
            ActionRegistry.register(GitClone::class) { name -> GitCloneConfig(name) }
        }
        scenario("When no target directory exists") {
            workspace("w") {
                project("p") {
                   action<GitClone>("g") {
                       repositoryUrl("git@gitlab.com:kohls/scps/scf/olm/olm-stubs.git")
                       targetDirectory("/tmp/olm-stubs")
                   }
                }
            } shouldBe Workspace(name = "w", projects = listOf(Project("p", actions = listOf(GitClone("g", repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/olm-stubs.git", targetDirectory = Directory("/tmp/olm-stubs"))))))
        }
    }
})
