package com.kohls.pws

import com.kohls.base.killPatterns
import com.kohls.base.nonExistingDirectory
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import org.mockito.ArgumentMatchers.contains
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
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
        }

        afterTest {
            ActionRegistry.unregister(LogFileEventuallyContains::class)
            ActionRegistry.unregister(Maven::class)
            ActionRegistry.unregister(GitClone::class)
            killPatterns("exec:java")
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
            workspace("w") {
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
                        duration = 5.seconds
                        searchedText = "INFO: Started Stub Server with port 8080"
                    }
                }
            }.execute()
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

