package com.kohls.pws.v2.tasks

import com.kohls.pws.v2.LookupTable
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File

class MavenCompilerTest : StringSpec({

    val original = Maven(args = emptyList(), variables = mutableMapOf(), background = false, settingsXmlFilePath = File("/tmp/settings.xml"), pomXmlFilePath = File("/tmp/pom.xml"), validations = emptyList())

    "Missing pom is derived from project source" { MavenCompiler().compile(original.copy(settingsXmlFilePath = null)) shouldBe original }
    "Missing settings is derived from project source" { MavenCompiler().compile(original.copy(pomXmlFilePath = null)) shouldBe original }
    "Nothing happens when all fields are populated" { MavenCompiler().compile(original) shouldBe original }
})

class MavenCompiler {
    val lookupTable = LookupTable()
    fun compile(maven: Maven): Maven {
        maven.pomXmlFilePath = maven.pomXmlFilePath ?: File("${lookupTable.findProjectSourcePath()}/pom.xml")
        maven.settingsXmlFilePath = maven.settingsXmlFilePath ?: File("${lookupTable.findProjectSourcePath()}/settings.xml")
        return maven
    }

}



