package com.kohls.pws

import com.kohls.base.Directory
import com.kohls.base.killPatterns
import io.kotest.core.spec.style.StringSpec
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

