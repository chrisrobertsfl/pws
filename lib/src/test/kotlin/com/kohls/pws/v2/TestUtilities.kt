package com.kohls.pws.v2



fun killPatterns(vararg patterns: String) {
    val killPattern = BashScriptFactory.fromResource(resourcePath = "/bash-scripts/kill-pattern.sh")
    patterns.forEach { killPattern.execute(it) }
}