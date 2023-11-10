package com.kohls.pws

import com.kohls.base.Directory
import org.mockito.Mockito
import java.io.File


fun killPatterns(vararg patterns: String) {
    val killPattern = BashScriptFactory.fromResource(resourcePath = "/bash-scripts/kill-pattern.sh")
    patterns.forEach { killPattern.execute(it) }
}

fun project(vararg names : String)  : Project {
    val name = names.first()
    val dependencies = names.drop(1)
    return Project(id = "project-$name", name = name, dependencies = dependencies, source = LocalSource(existingDirectory()), tasks = listOf(), parallel = true)
}


fun existingFile(): File {
    val file = Mockito.mock(File::class.java)
    Mockito.`when`(file.exists()).thenReturn(true)
    return file
}

fun nonExistingFile(path: String): File {
    val file = Mockito.mock(File::class.java)
    Mockito.`when`(file.exists()).thenReturn(false)
    Mockito.`when`(file.absolutePath).thenReturn(path)
    return file
}

fun existingDirectory(): Directory {
    val directory = Mockito.mock(Directory::class.java)
    Mockito.`when`(directory.exists()).thenReturn(true)
    return directory
}

fun nonExistingDirectory(path: String): Directory {
    val directory = Mockito.mock(Directory::class.java)
    Mockito.`when`(directory.exists()).thenReturn(false)
    Mockito.`when`(directory.path).thenReturn(path)
    return directory
}
