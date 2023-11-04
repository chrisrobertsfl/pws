package com.kohls.pws


fun killPatterns(vararg patterns: String) {
    val killPattern = BashScriptFactory.fromResource(resourcePath = "/bash-scripts/kill-pattern.sh")
    patterns.forEach { killPattern.execute(it) }
}

fun project(vararg names : String)  : Project {
    val name = names.first()
    val dependencies = names.drop(1)
    return Project(id = "project-$name", name = name, dependencies = dependencies, source = LocalSource(existingDirectory()), tasks = listOf(), parallel = true)
}