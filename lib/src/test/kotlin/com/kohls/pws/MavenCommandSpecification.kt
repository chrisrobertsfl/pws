package com.kohls.pws

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds

class MavenCommandSpecification : StringSpec({
    afterTest {
        killPattern("exec:java")
    }

    "execute command successfully" {
        val cmd = MavenCommand(
            background = true, declaredVariables = mapOf(
                "args" to "-U clean install exec:java", "runDirectory" to "/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs"
            ), environmentVariables = mapOf("HTTPS_PROXY" to "http://proxy.kohls.com:3128"), validations = listOf(
                LogValidator(
                    duration = 10.seconds, contains = listOf("INFO: Started Stub Server with port 8080")
                )
            )
        )
        cmd.initialize()
        println(cmd.bashScript.contents())
        cmd.perform() shouldBe true
    }

})