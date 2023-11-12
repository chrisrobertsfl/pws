package com.kohls.pws

import com.kohls.base.Directory
import com.kohls.base.killPatterns
import com.kohls.pws.Action.Companion.generateName
import io.kotest.core.spec.style.StringSpec
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.time.Duration.Companion.seconds

class OlmStubsCloneRunAndHealthcheck : StringSpec({

    // TODO:  Change from mutable list to regular one
    // TODO:  Make this more of a unit test also
    // TODO:  Check to make sure that the settings.xml file exists if present otherwise fail
    // TODO:  Check to make sure that the pom.xml file exists if present otherwise fail
    "run simple maven action against olm-stubs" {
        val gitClone = GitClone(repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/olm-stubs.git", targetDirectory = Directory("/tmp/workspace/olm-stubs"))
        val maven = Maven(
            settingsXmlFile = File("/Users/TKMA5QX/data/repo/maven/settings.xml"),
            goals = mutableListOf("clean", "install", "exec:java"),
        )
        val fileEventuallyContains = FileEventuallyContains(duration = 10.seconds, searchedText = "8080")
        var parameters = gitClone.perform()
        parameters = maven.perform(parameters)
        fileEventuallyContains.perform(parameters)
    }

    afterTest {
        killPatterns("exec:java")
    }
})

data class GitClone(override val name: String = generateName(), val repositoryUrl: String, val targetDirectory: Directory, val overwrite: Boolean = true) : Action {
    private val logger: Logger = LoggerFactory.getLogger(Maven::class.java)
    override fun perform(parameters: Parameters): Parameters {
        if (targetDirectory.exists()) {
            when (overwrite) {
                true -> targetDirectory.delete()
                false -> throw IllegalStateException("targetDirectory '${targetDirectory.path}' already exists and cannot be overwritten when overwrite is enabled")
            }
        }
        val executableScript = BashScript(commandName = "git-clone", body = Body.fromResource("/bash-scripts/git-clone.sh")).createExecutableScript()
        executableScript.execute(listOf(repositoryUrl, targetDirectory.path))
        return Parameters.create("targetDirectory" to targetDirectory)
    }

}

