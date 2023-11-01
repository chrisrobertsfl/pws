package com.kohls.pws.v2

import io.kotest.matchers.string.shouldContain

private infix fun BashScript.logShouldContain(text: String) = logContents().joinToString("\n") shouldContain text


private fun bashScript() = BashScriptFactory.fromLines(
    variables = mapOf("HTTPS_PROXY" to "http://proxy.kohls.com:3128"), lines = listOf(
        "(( ${'$'}{#} == 2 )) || { echo \"usage ${'$'}{0} <name> <exit-code>\"; exit 1; }", "echo hello \${1}", "exit ${'$'}{2}"
    )
)
