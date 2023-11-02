package com.kohls.pws.v2.tasks

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File

class MavenCompilerTest : StringSpec({

    "Missing pom is derived from project source" {
        MavenCompiler().compile(
            Maven(args = listOf(), variables = mutableMapOf(), background = false, settingsXmlFilePath = File(""), pomXmlFilePath = null, validations = listOf())
        ) shouldBe Maven(args = listOf(), variables = mutableMapOf(), background = false, settingsXmlFilePath = File(""), pomXmlFilePath = File("/project/pom.xml"), validations = listOf())
    }

    "Nothing happens when all fields are populated" {
        MavenCompiler().compile(
            Maven(args = listOf(), variables = mutableMapOf(), background = false, settingsXmlFilePath = File(""), pomXmlFilePath = File("/tmp/pom.xml"), validations = listOf())
        ) shouldBe Maven(args = listOf(), variables = mutableMapOf(), background = false, settingsXmlFilePath = File(""), pomXmlFilePath = File("/tmp/pom.xml"), validations = listOf())
    }
})

class MavenCompiler {
        val lookupTable = LookupTable()
        fun compile(maven: Maven): Maven {
            if (maven.pomXmlFilePath == null) {
                val projectSourcePath = lookupTable.findProjectSourcePath()
                val pomXmlFilePath = File("${projectSourcePath}/pom.xml")
                maven.pomXmlFilePath = pomXmlFilePath
            }
            return maven        }

}

class LookupTable {
    fun findProjectSourcePath(): String {
        return "/project"
    }

}