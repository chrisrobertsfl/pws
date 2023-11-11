package com.kohls.pws

import com.kohls.base.Eventually
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.time.Duration.Companion.seconds

class MavenSpecification : StringSpec({

    // TODO:  Change from mutable list to regular one
    // TODO:  Make this more of a unit test also
    "run simple maven action against olm-stubs" {
        val maven = Maven(
            name = "maven olm-stubs service start",
            pomXmlFile = File("/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs/pom.xml"),
            settingsXmlFile = File("/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs/pom.xml"),
            goals = mutableListOf("clean", "install", "exec:java"),
        )
        val logFile = maven.perform()["logFile"] as LogFile


        Eventually(condition = {
            logFile.validate { it.readText().contains("8080") }
        }).satisfiedWithin(duration = 10.seconds) shouldBe true
    }
})