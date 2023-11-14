package com.kohls.pws

import com.kohls.base.killPatterns
import com.kohls.base.nonExistingDirectory
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import org.mockito.ArgumentMatchers.contains
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.slf4j.Logger
import kotlin.time.Duration.Companion.seconds

class DslFeatureSpecification : FeatureSpec({

    feature("When something goes wrong, during execution") {
        scenario("Directory for action does not exist") {
            val action = ValidDirectoryExists(directory = nonExistingDirectory("/default/project"))
            val project = Project(actions = listOf(action))
            val workspace = Workspace(projects = listOf(project), logger = mock(Logger::class.java))
            workspace.execute()
            verify(workspace.logger).error("Invalid directory /default/project")
        }
    }

    feature("Olm Stubs Run") {
        beforeTest {
            ActionRegistry.register(GitClone::class) { name -> GitClone(name) }
            ActionRegistry.register(Maven::class) { name -> Maven(name) }
            ActionRegistry.register(LogFileEventuallyContains::class) { name -> LogFileEventuallyContains(name) }
            ActionRegistry.register(GitCheckout::class) { name -> GitCheckout(name) }
            ActionRegistry.register(TextResponseHealthCheck::class) { name -> TextResponseHealthCheck(name) }
        }

        afterTest {
            ActionRegistry.unregister(TextResponseHealthCheck::class)
            ActionRegistry.unregister(GitCheckout::class)
            ActionRegistry.unregister(LogFileEventuallyContains::class)
            ActionRegistry.unregister(Maven::class)
            ActionRegistry.unregister(GitClone::class)
            //killPatterns("exec:java")
        }

        scenario("Clone it") {
            val actualWorkspace = workspace("w") {
                project("p") {
                    action<GitClone>("g") {
                        targetDirectoryPath = "/tmp/workspace/olm-stubs"
                        repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/olm-stubs.git"
                    }
                }
            }
            actualWorkspace shouldBe Workspace("w", projects = listOf(Project("p", listOf(GitClone("g")))))
            actualWorkspace.execute()
        }

        // TODO:  Set up fixture so that the project already exists as a unit test
        scenario("Run it with everything specified") {

            val mockLogger = mock(Logger::class.java)
            val workspace = workspace("w") {
                project("olm-stubs") {
                    action<GitClone>("olm-stubs git clone") {
                        targetDirectoryPath = "/tmp/workspace/olm-stubs"
                        repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/olm-stubs.git"
                    }
                    action<Maven>("olm-stubs run maven") {
                        pomXmlFilePath = "/tmp/workspace/olm-stubs/pom.xml"
                        settingsXmlFilePath = "/Users/TKMA5QX/data/repo/maven/settings.xml"
                        goals += "install"
                        goals += "exec:java"
                    }
                    action<LogFileEventuallyContains>("olm-stubs check maven") {
                        duration = 10.seconds
                        searchedText = "INFO: Started Stub Server with port 8080"
                    }
                    action<TextResponseHealthCheck>("olm-stubs service healthcheck at port 8080") {
                        url = "http://localhost:8080"
                        searchedText = "StubServer is running."
                        attempts = 5
                        interval = 1.seconds
                    }
                }
                project("store-fulfillment") {
                    action<GitClone>("store-fulfillment git clone") {
                        targetDirectoryPath = "/tmp/workspace/store-fulfillment"
                        repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/store-fulfillment.git"
                    }
                    action<GitCheckout>("store-fulfillment git checkout:  OMO-1914") {
                        targetDirectoryPath = "/tmp/workspace/store-fulfillment"
                        branchName = "OMO-1914"
                    }
                }
            }
            println("workspace = ${workspace}")
            //workspace.apply { logger = mockLogger }.execute()
            workspace.execute()
            //verify(mockLogger, never()).error(contains("ERROR"))

            // TODO: Check that it does not contain any errors in the log
        }

        scenario("Run it with pom missing which should inherit from previous action") {
            workspace("w") {
                project("p") {
                    action<GitClone>("g") {
                        targetDirectoryPath = "/tmp/workspace/olm-stubs"
                        repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/olm-stubs.git"
                    }
                    action<Maven>("m") {
                        settingsXmlFilePath = "/Users/TKMA5QX/data/repo/maven/settings.xml"
                        goals += "install"
                        goals += "exec:java"
                    }
                }
            }.execute()
        }

        scenario("Fail when pom missing completely") {
            val workspace = workspace("w") {
                project("p") {
                    action<Maven>("m") {
                        settingsXmlFilePath = "/Users/TKMA5QX/data/repo/maven/settings.xml"
                        goals += "install"
                        goals += "exec:java"
                    }
                }
            }
            workspace.apply { logger = mock(Logger::class.java) }.execute()
            verify(workspace.logger).error("Missing pomXmlFilePath")
        }

        scenario("Fail when settings missing completely with pom there") {
            val workspace = workspace("w") {
                project("p") {
                    action<GitClone>("g") {
                        targetDirectoryPath = "/tmp/workspace/olm-stubs"
                        repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/olm-stubs.git"
                    }
                    action<Maven>("m") {
                        pomXmlFilePath = "/tmp/workspace/olm-stubs/pom.xml"
                        goals += "install"
                        goals += "exec:java"
                    }
                }
            }
            workspace.apply { logger = mock(Logger::class.java) }.execute()
            verify(workspace.logger).error("Missing settingsXmlFilePath")
        }

        scenario("Fail when settings missing completely with pom derived from previous action") {
            val workspace = workspace("w") {
                project("p") {
                    action<GitClone>("g") {
                        targetDirectoryPath = "/tmp/workspace/olm-stubs"
                        repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/olm-stubs.git"
                    }
                    action<Maven>("m") {
                        goals += "install"
                        goals += "exec:java"
                    }
                }
            }
            workspace.apply { logger = mock(Logger::class.java) }.execute()
            verify(workspace.logger).error("Missing settingsXmlFilePath")
        }


        scenario("Run it with everything specified - except time out quickly that that it cannot find log file searched text in time") {
            val workspace = workspace("w") {
                project("p") {
                    action<GitClone>("g") {
                        targetDirectoryPath = "/tmp/workspace/olm-stubs"
                        repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/olm-stubs.git"
                    }
                    action<Maven>("m") {
                        pomXmlFilePath = "/tmp/workspace/olm-stubs/pom.xml"
                        settingsXmlFilePath = "/Users/TKMA5QX/data/repo/maven/settings.xml"
                        goals += "install"
                        goals += "exec:java"
                    }
                    action<LogFileEventuallyContains>("l") {
                        duration = 0.seconds
                        searchedText = "INFO: Started Stub Server with port 8080"
                    }
                }
            }
            workspace.apply { logger = mock(Logger::class.java) }.execute()
            verify(workspace.logger).error(contains("Could not find 'INFO: Started Stub Server with port 8080'"))

        }
    }

})

