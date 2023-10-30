package com.kohls.pws

fun killPattern(pattern: String) {
    val bashScript = BashScript.from(
        resourcePath = "/bash-scripts/kill-pattern.sh", declaredVariables = mapOf("pattern" to pattern)
    )
    bashScript.execute()
}