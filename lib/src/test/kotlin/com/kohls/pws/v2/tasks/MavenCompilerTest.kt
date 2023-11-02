package com.kohls.pws.v2.tasks

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File

class MavenCompilerTest : StringSpec({

    val original = Maven(args = listOf(), variables = mutableMapOf(), background = false, settingsXmlFilePath = File("/tmp/.m2/settings.xml"), pomXmlFilePath = File("/tmp/pom.xml"), validations = listOf())

    "Missing pom is derived from project source" { MavenCompiler().compile(original.copy(pomXmlFilePath = null)) shouldBe original }

    "Nothing happens when all fields are populated" { MavenCompiler().compile(original) shouldBe original }
})

class MavenCompiler {
    val lookupTable = LookupTable()
    fun compile(maven: Maven): Maven {
        maven.pomXmlFilePath = maven.pomXmlFilePath ?: File("${lookupTable.findProjectSourcePath()}/pom.xml")
        return maven
    }

}

class LookupTable {
    fun findProjectSourcePath(): String {
        return "/tmp"
    }

}