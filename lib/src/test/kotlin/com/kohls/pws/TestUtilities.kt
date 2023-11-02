package com.kohls.pws


fun killPatterns(vararg patterns: String) {
    val killPattern = BashScriptFactory.fromResource(resourcePath = "/bash-scripts/kill-pattern.sh")
    patterns.forEach { killPattern.execute(it) }
}