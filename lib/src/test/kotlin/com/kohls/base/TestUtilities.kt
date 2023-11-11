package com.kohls.base

import com.google.common.io.Resources
import com.kohls.pws2.scripting.BashScript
import com.kohls.pws2.scripting.Body
import com.kohls.pws2.scripting.ExecutableScript
import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import org.mockito.Mockito
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

val NEW_LINE: String = System.lineSeparator()

object CustomMultilineStyle : ToStringStyle() {
    private fun readResolve(): Any = CustomMultilineStyle

    init {
        this.contentStart = "[$NEW_LINE "
        this.fieldSeparator = " "
        this.isFieldSeparatorAtStart = true
        this.contentEnd = "$NEW_LINE]"
        this.isUseShortClassName = true
        this.isUseIdentityHashCode = false
    }
}

val logger : Logger = LoggerFactory.getLogger("Test Utilities")
inline fun <reified T> T.multilinePrint(typeName: String? = T::class.simpleName) {
    val multilineString = try {
        ReflectionToStringBuilder.toString(this, CustomMultilineStyle)
    }catch(e : Exception) {
        this.toString()
    }
    logger.trace("$typeName = $multilineString")
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

fun existingDirectory(path: String = "/default/path"): Directory {
    val directory = Mockito.mock(Directory::class.java)
    Mockito.`when`(directory.exists()).thenReturn(true)
    Mockito.`when`(directory.path).thenReturn(path)

    return directory
}

fun nonExistingDirectory(path: String): Directory {
    val directory = Mockito.mock(Directory::class.java)
    Mockito.`when`(directory.exists()).thenReturn(false)
    Mockito.`when`(directory.path).thenReturn(path)
    return directory
}

fun tempFileFromResource(resourcePath: String): File = kotlin.io.path.createTempFile().toFile().apply {
    writeText(Resources.getResource(resourcePath).readText())
}

val killPattern : ExecutableScript = BashScript(commandName = "kill-pattern", body = Body.fromResource("/bash-scripts/kill-pattern.sh")).createExecutableScript()
fun killPatterns(vararg args : String) {
    args.toList().forEach { killPattern.execute(it) }
}