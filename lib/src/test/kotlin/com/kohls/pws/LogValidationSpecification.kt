package com.kohls.pws

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.time.Duration.Companion.seconds

class LogValidationSpecification : StringSpec({

    "log contains something I need" {
        LogValidator(
            duration = 5.seconds, contains = listOf(
                "[INFO] -------------------------< com.olm:olm-stubs >--------------------------", "[INFO] --- clean:3.2.0:clean (default-clean) @ olm-stubs ---"
            )
        ).validate(
            args = mapOf(
                "abc" to "def", "logFile" to File("/tmp/olm-stubs.log")
            )
        ) shouldBe true
    }
})