package com.kohls.pws

import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.test.Ignore
import kotlin.time.Duration.Companion.seconds


@Ignored("Must be a unit test")
class LogValidationSpecification : StringSpec({

    "log contains something I need" {
        LogValidator(
            duration = 5.seconds, contains = listOf(
                "[INFO] -------------------------< com.olm:olm-stubs >--------------------------", "[INFO] --- clean:3.2.0:clean (default-clean) @ olm-stubs ---"
            )
        ).validate(args = mapOf("logFile" to File("/tmp/olm-stubs.log"))) shouldBe true
    }
})