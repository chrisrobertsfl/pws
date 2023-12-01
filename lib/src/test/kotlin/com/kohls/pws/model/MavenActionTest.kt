package com.kohls.pws.model

import com.kohls.base.Directory
import com.kohls.pws.BashScript
import com.kohls.pws.ExecutableScript
import com.kohls.pws.LogFile
import com.kohls.pws.Parameters
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Path


class MavenActionTest : FeatureSpec({

    feature("Maven Action") {
        scenario("Successful Maven execution with valid paths") {
            val name = mockk<ActionName>(relaxed = true)
            val pom = mockk<XmlFile>(relaxed = true)
            val settings = mockk<XmlFile>(relaxed = true)
            val target = mockk<Directory>()
            val path = mockk<Path>(relaxed = true)
            every { target.existsOrThrow(any()) } returns target
            every { target.path } returns path
            val bashScript = mockk<BashScript>()
            val goals = mockk<MavenGoals>(relaxed = true)
            val action = MavenAction(name, target, pom, settings, goals, bashScript)
            val logFile = mockk<LogFile>(relaxed = true)
            val executableScript = mockk<ExecutableScript>(relaxed = true)
            every { executableScript.logFile } returns logFile
            every { bashScript.createExecutableScript(background = true, workingDirectory = target) } returns executableScript
            action.perform(Parameters.EMPTY) shouldBe Parameters.create("logFile" to logFile)
        }

        scenario("Maven execution with missing pom.xml file path") {

        }

        scenario("Maven execution with missing settings.xml file path") {

        }

        scenario("Maven execution with non-existent working directory") {

        }

    }
})
