package com.kohls.pws

import com.kohls.base.Directory
import com.kohls.base.killPatterns
import com.kohls.base.nonExistingDirectory
import com.kohls.base.randomPathName
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import org.mockito.ArgumentMatchers.contains
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

class DslFeatureSpecification : FeatureSpec({

    feature("When something goes wrong, during execution") {
        scenario("Directory for action does not exist") {
            val action = ValidDirectoryExists(directory = Directory(randomPathName()))
            val project = Project(actions = listOf(action))
            val workspace = Workspace(projects = listOf(project), logger = mock(Logger::class.java))
            workspace.execute()
            verify(workspace.logger).error("Invalid directory ${action.directory.toFile().absolutePath}")
        }
    }

    feature("Olm Stubs Run") {
        beforeTest {
            ActionRegistry.register(GitClone::class) { name -> GitClone(name) }
            ActionRegistry.register(Maven::class) { name -> Maven(name) }
            ActionRegistry.register(LogFileEventuallyContains::class) { name -> LogFileEventuallyContains(name) }
            ActionRegistry.register(GitCheckout::class) { name -> GitCheckout(name) }
            ActionRegistry.register(TextResponseHealthCheck::class) { name -> TextResponseHealthCheck(name) }
            ActionRegistry.register(JsonResponseHealthCheck::class) { name -> JsonResponseHealthCheck(name) }
            ActionRegistry.register(GitPrepare::class) { name -> GitPrepare(name) }
            ActionRegistry.register(OlmMavenSpringService::class) { name -> OlmMavenSpringService(name) }
        }

        afterTest {
            ActionRegistry.unregister(OlmMavenSpringService::class)
            ActionRegistry.unregister(GitPrepare::class)
            ActionRegistry.unregister(JsonResponseHealthCheck::class)
            ActionRegistry.unregister(TextResponseHealthCheck::class)
            ActionRegistry.unregister(GitCheckout::class)
            ActionRegistry.unregister(LogFileEventuallyContains::class)
            ActionRegistry.unregister(Maven::class)
            ActionRegistry.unregister(GitClone::class)
            //killPatterns("exec:java", "spring-boot:run")
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

//        // TODO:  Set up fixture so that the project already exists as a unit test
//        scenario("Run it with everything specified") {
//
//            val mockLogger = mock(Logger::class.java)
//            doAnswer { invocation ->
//                val message = invocation.getArgument<String>(0)
//                LoggerFactory.getLogger(Workspace::class.java).info(message)
//                null // since it's a void method
//            }.whenever(mockLogger).info(anyString())
//
//            val workspace = workspace("w") {
//
//                targetParentPath("/tmp/workspace")
//                settingsXmlFilePath("/Users/TKMA5QX/data/repo/maven/settings.xml")
//
//                project("olm-stubs") {
//                    action<GitClone>("olm-stubs git clone") {
//                        targetDirectoryPath = "/tmp/workspace/olm-stubs"
//                        repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/olm-stubs.git"
//                    }
//                    action<Maven>("olm-stubs starts via maven") {
//                        pomXmlFilePath = "/tmp/workspace/olm-stubs/pom.xml"
//                        settingsXmlFilePath = "/Users/TKMA5QX/data/repo/maven/settings.xml"
//                        workingDirectoryPath = "/tmp/workspace/olm-stubs"
//                        goals += "install"
//                        goals += "exec:java"
//                    }
//                    action<LogFileEventuallyContains>("olm-stubs is running via maven") {
//                        initialDelay = 1.seconds
//                        duration = 10.seconds
//                        searchedText = "INFO: Started Stub Server with port 8080"
//                    }
//                    action<TextResponseHealthCheck>("olm-stubs service healthcheck at port 8080") {
//                        url = "http://localhost:8080"
//                        searchedText = "StubServer is running."
//                        attempts = 5
//                        interval = 1.seconds
//                    }
//                }
//                project("config-server") {
//                    action<GitClone>("config-server git clone") {
//                        targetDirectoryPath = "/tmp/workspace/config-server"
//                        repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/config-server.git"
//                    }
//                    action<Maven>("config-server starts via maven") {
//                        pomXmlFilePath = "/tmp/workspace/config-server/pom.xml"
//                        settingsXmlFilePath = "/Users/TKMA5QX/data/repo/maven/settings.xml"
//                        workingDirectoryPath = "/tmp/workspace/config-server"
//                        goals += "spring-boot:run"
//                    }
//                    action<LogFileEventuallyContains>("config-server is running via maven") {
//                        initialDelay = 3.seconds
//                        duration = 10.seconds
//                        searchedText = "Started ConfigServerApplication in"
//                    }
//                    action<JsonResponseHealthCheck>("config-server service healthcheck at port 5001") {
//                        url = "http://localhost:5001/actuator/health"
//                        searchedField = "status" to "UP"
//                        attempts = 10
//                        interval = 1.seconds
//                    }
//                }
//
//                project("Store Fulfillment") {
//                    action<OlmMavenSpringService>("Store Fulfillment Service") {
//                        repositoryName = "store-fulfillment"
//                        applicationName = "StoreFulfillmentApplication"
//                        healthcheckPort = 8090
//                    }
//                }

//                project("store-fulfillment") {
//                    action<GitPrepare>("store-fulfillment git prepare") {
//                        targetDirectoryPath = "/tmp/workspace/store-fulfillment"
//                        repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/store-fulfillment.git"
//                        branchName = "test-branch"
//                    }
//                    action<Maven>("store-fulfillment starts via maven") {
//                        pomXmlFilePath = "/tmp/workspace/store-fulfillment/pom.xml"
//                        settingsXmlFilePath = "/Users/TKMA5QX/data/repo/maven/settings.xml"
//                        workingDirectoryPath = "/tmp/workspace/store-fulfillment"
//                        goals += "spring-boot:run"
//                    }
//                    action<LogFileEventuallyContains>("store-fulfillment is running via maven") {
//                        initialDelay = 3.seconds
//                        duration = 30.seconds
//                        searchedText = "Started StoreFulfillmentApplication in"
//                    }
//                    action<JsonResponseHealthCheck>("store-fulfillment service healthcheck at port 8090") {
//                        url = "http://localhost:8090/actuator/health"
//                        searchedField = "status" to "UP"
//                        attempts = 10
//                        interval = 1.seconds
//                    }
//                }
//
//                project("order-create") {
//                    action<OlmMavenSpringService>("order-create") {
//                        repositoryName = "order-create"
//                        applicationName = "OrderCreateApplication"
//                        healthcheckPort = 8083
//                   }
//                }
//            }
//            workspace.apply { logger = mockLogger }.execute()
//            //workspace.execute()
//            verify(mockLogger, never()).error(any())
//        }

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
                        duration = 0.1.seconds
                        searchedText = "INFO: Started Stub Server with port 8080"
                    }
                }
            }
            workspace.apply { logger = mock(Logger::class.java) }.execute()
            verify(workspace.logger).error(contains("Could not find 'INFO: Started Stub Server with port 8080'"))

        }
    }

})

