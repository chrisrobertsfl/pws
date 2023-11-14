package com.kohls.pws

import com.kohls.base.killPatterns
import io.kotest.core.spec.style.StringSpec
import kotlin.time.Duration.Companion.seconds

class TextResponseHealthCheckTest : StringSpec({

    beforeTest {
        Maven().apply {
            pomXmlFilePath = "/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs/pom.xml"
            settingsXmlFilePath = "/Users/TKMA5QX/data/repo/maven/settings.xml"
            workingDirectoryPath = "/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs"
            goals += "exec:java"
        }.perform()
    }
    "Check olm-stubs running locally" {
        TextResponseHealthCheck(
            name = "name", url = "http://localhost:8080", searchedText = "StubServer is running.", ignoreCase = true, attempts = 40, interval = 1.seconds, initialDelay = 5.seconds
        ).perform()
    }

    afterTest {
        killPatterns("exec:java")
    }
})