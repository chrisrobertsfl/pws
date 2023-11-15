package com.kohls.pws

import com.kohls.base.*
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import java.io.File
import kotlin.time.Duration.Companion.seconds

class BashScriptSpecification : FeatureSpec({

    val bashScript = BashScript(
        commandName = "maven",
        shebang = Shebang("/bin/bash"),
        environmentVariables = listOf(EnvironmentVariable("HTTPS_PROXY", "http://proxy.kohls.com:3128")),
        body = Body.fromResource(resourcePath = "/bash-scripts/bash-maven.sh"),
    )

    feature("Rendering Contents") {
        scenario("renders script contents") {
            val expectedContents = """
                #!/bin/bash
                export HTTPS_PROXY="http://proxy.kohls.com:3128"

                (( ${'$'}{#} == 0 ))  &&  { echo "Usage: ${'$'}{0} <pom-file-path> <settings-xml-file-path> <args>"; exit 1; }
                declare pomFilePath=${'$'}{1}
                declare settingsXmlFilePath=${'$'}{2}
                echo pomFilePath is ${'$'}pomFilePath
                echo settingsXmlFilePath is ${'$'}settingsXmlFilePath
                shift 2
                echo args are "${'$'}{@}"
                mvn --file ${'$'}{pomFilePath} --settings ${'$'}{settingsXmlFilePath} "${'$'}{@}" 2>&1
                
            """.trimIndent()

            bashScript.render() shouldBe expectedContents
        }
    }

    // TODO: FIX BROKEN TEST
    feature("Creating Executable Script") {
//        scenario("generates maven script file") {
//            val fileGenerator = fileGenerator("/tmp/maven-bash-script.sh")
//            val mavenBashScript = bashScript.copy(scriptFileGenerator = fileGenerator)
//            val file = fileGenerator.generate()
//            println("file      = ${file}")
//            println("file.name = ${file.name}")
//            val executableScript = ExecutableScript(scriptFile = ScriptFile(file = file), logFile = LogFile(file = existingFile()))
//            mavenBashScript.createExecutableScript().scriptFile shouldBe executableScript.scriptFile
//            mavenBashScript.render() shouldBe executableScript.scriptContents()
//            executableScript.multilinePrint("executableScript")
//        }

        scenario("generates maven log file") {
            val fileGenerator = fileGenerator("/tmp/maven-bash-script.log")
            val mavenBashScript = bashScript.copy(logFileGenerator = fileGenerator)
            val executableScript = ExecutableScript(scriptFile = ScriptFile(file = existingFile()), logFile = LogFile(file = fileGenerator.generate()))
            executableScript.multilinePrint("executableScript")
            mavenBashScript.createExecutableScript().logFile shouldBe executableScript.logFile
        }


        scenario("generates script file using default") {
            val mavenBashScript = bashScript.copy(commandName = "maven")
            val executableScript = mavenBashScript.createExecutableScript()
            executableScript.multilinePrint()
            executableScript.scriptFile.validate { it.name.startsWith("maven") && it.name.endsWith(".sh") } shouldBe true
        }

        scenario("generates log file using default") {
            val mavenBashScript = bashScript.copy(commandName = "maven")
            val executableScript = mavenBashScript.createExecutableScript()
            executableScript.multilinePrint()
            executableScript.logFile.validate { it.name.startsWith("maven") && it.name.endsWith(".log") } shouldBe true
        }

    }

    feature("Running Executable") {

        scenario("Execute the script for olm-stubs") {
            val executableScript = bashScript.createExecutableScript(background = true)
            executableScript.execute(listOf("/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs/pom.xml", "/Users/TKMA5QX/data/repo/maven/settings.xml", "clean", "install", "exec:java"))
            Eventually { executableScript.logFile.validate { it.readText().contains("8080") } }.isMetWithin(TimeFrame(10.seconds)) shouldBe true
        }
    }

    afterTest {
        killPatterns("exec:java")
    }
})

private fun fileGenerator(path: String): FileGenerator {
    val fileGenerator: FileGenerator = mock(FileGenerator::class.java)
    val file = mock(File::class.java)
    `when`(fileGenerator.generate()).thenReturn(file)
    `when`(file.name).thenReturn(path.substringAfterLast("/"))
    return fileGenerator
}
