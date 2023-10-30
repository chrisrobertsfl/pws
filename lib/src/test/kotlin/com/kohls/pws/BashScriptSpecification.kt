package com.kohls.pws

import com.kohls.pws.BashScript.Companion.from
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class BashScriptSpecification : StringSpec({

    val bashScript = from(
        lines = listOf("echo hello \${1} \$HTTPS_PROXY", "exit 1"), environmentVariables = mapOf("HTTPS_PROXY" to "http://proxy.kohls.com:3128"),
    )
    "check that bash script is well formed" {
        bashScript.contents() shouldBe """
            #!/bin/bash
            export HTTPS_PROXY="http://proxy.kohls.com:3128"
            echo hello ${'$'}{1} ${'$'}HTTPS_PROXY
            exit 1
        """.trimIndent()
    }

    "check that bash script exits with code 1 and output is what is expected" {
        val bs = BashScript.from(lines = listOf("echo hello \${1} \$HTTPS_PROXY", "exit 1"), environmentVariables = mapOf("HTTPS_PROXY" to "http://proxy.kohls.com:3128"))
        bs.execute(listOf("Chris")) shouldBe 1
        bs.logContents() shouldContainExactly listOf("hello Chris http://proxy.kohls.com:3128")
    }

    "check that bash script exits with code 0 and output is what is expected" {
        val bs = BashScript.from(lines = listOf("echo hello \${1} \$HTTPS_PROXY", "exit 0"), environmentVariables = mapOf("HTTPS_PROXY" to "http://proxy.kohls.com:3128"))
        bs.execute(listOf("Chris")) shouldBe 0
        bs.logContents() shouldContainExactly listOf("hello Chris http://proxy.kohls.com:3128")
    }

})