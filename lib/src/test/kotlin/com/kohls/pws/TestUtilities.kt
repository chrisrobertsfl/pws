package com.kohls.pws

fun killPattern(pattern: String) = BashScript.from(
    resourcePath = "/bash-scripts/kill-pattern.sh", declaredVariables = mapOf("pattern" to pattern)
).execute()

fun killPatterns(vararg patterns: String) = patterns.forEach { killPattern(it) }