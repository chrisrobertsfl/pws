package com.kohls.pws

import io.kotest.core.spec.style.StringSpec
import java.lang.Thread.sleep
import kotlin.time.Duration.Companion.milliseconds

class TextResponseHealthCheckTest : StringSpec({

    beforeTest {
        Maven().apply {
            pomXmlFilePath = "/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs"
            settingsXmlFilePath = "/Users/TKMA5QX/data/repo/maven/settings.xml"
            goals += "exec:java"
        }
            .perform()
    }
    "Check olm-stubs running locally" {

        sleep(5000000)
//        sleep(5000)
//        TextResponseHealthCheck(
//            name = "name",
//            url = "http://localhost:8080",
//            searchedText = "StubServer is running.",
//            ignoreCase = true,
//            attempts = 40,
//            interval = 250.milliseconds
//        ).perform()

    }
}) {
}