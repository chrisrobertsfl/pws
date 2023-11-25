package com.kohls.base

import com.google.common.io.Resources
import com.kohls.base.Generators.pathNames
import com.kohls.base.Generators.pick
import com.kohls.pws.BashScript
import com.kohls.pws.Body
import com.kohls.pws.ExecutableScript
import io.kotest.assertions.Exceptions
import io.kotest.assertions.clueContextAsString
import io.kotest.assertions.errorCollector
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.TimeoutCancellationException
import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import org.instancio.Gen.ints
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

val logger: Logger = LoggerFactory.getLogger("Test Utilities")
inline fun <reified T> T.multilinePrint(typeName: String? = T::class.simpleName) {
    val multilineString = try {
        ReflectionToStringBuilder.toString(this, CustomMultilineStyle)
    } catch (e: Exception) {
        this.toString()
    }
    logger.trace("$typeName = $multilineString")
}


fun existingFile(): File {
    val file = Mockito.mock(File::class.java)
    Mockito.`when`(file.exists()).thenReturn(true)
    Mockito.`when`(file.name).thenReturn("name")
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
    //TODO:  Fix this:
    // Mockito.`when`(directory.path).thenReturn(path)

    return directory
}

fun nonExistingDirectory(path: String): Directory {
    val directory = Mockito.mock(Directory::class.java)
    Mockito.`when`(directory.exists()).thenReturn(false)
    // TODO:  Fix this
    //Mockito.`when`(directory.path).thenReturn(path)
    return directory
}

val killPattern: ExecutableScript = BashScript(commandName = "kill-pattern", body = Body.fromResource("/bash-scripts/kill-pattern.sh")).createExecutableScript()
fun killPatterns(vararg args: String) {
    args.toList().forEach { killPattern.execute(listOf(it)) }
}

fun File.addChildDirectory(suffix: String = ""): File = addChild(Generators.filename() + suffix) {mkdir() }
fun File.addChildFile(extension: String = Generators.extension()): File  = addChild(Generators.filename() + extension) { createNewFile() }
fun File.addChild(name : String, block : (File) -> Unit) : File {
    withAssumption({ "$absolutePath should be a directory" }) { isDirectory() shouldBe true }
    return File(this, name).apply(block)
}

fun randomPathName(min : Int = 1, max : Int = 5) : String {
    val depth = ints().range(min, max).get()
    return pick(pathNames, depth).joinToString(prefix = "/", separator = "/")
}
inline fun <R> withAssumption(crossinline clue: () -> Any?, thunk: () -> R): R {
    val collector = errorCollector
    try {
        collector.pushClue {
            buildString {
                append("Assumption failed: '")
                append(clue.invoke().toString())
                append("'")
            }
        }
        return thunk()
        // this is a special control exception used by coroutines
    } catch (t: TimeoutCancellationException) {
        throw Exceptions.createAssertionError(clueContextAsString() + (t.message ?: ""), t)
    } finally {
        collector.popClue()
    }
}
