package com.kohls.pws

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import java.io.File

class LogSpecification : StringSpec({

    "Log file should be created if not exists" {
        val log = Log("/tmp/testLog1.log") {}
        log.file.exists() shouldBe true
    }

    "Log file should consume lines" {
        val consumedLines = mutableListOf<String>()
        val log = Log("/tmp/testLog2.log") { line -> consumedLines.add(line) }
        File("/tmp/testLog2.log").writeText("Line1\nLine2\n")
        log.consumeLines()
        runBlocking { delay(500) }
        consumedLines shouldBe listOf("Line1", "Line2")
    }


    "Log file should be empty initially" {
        val log = Log("/tmp/testLog3.log") {}
        log.file.readText() shouldBe ""
    }

    "Log file should append lines when consumed" {
        val consumedLines = mutableListOf<String>()
        val log = Log("/tmp/testLog4.log") { line -> consumedLines.add(line) }
        File("/tmp/testLog4.log").writeText("Line1\n")
        log.consumeLines()
        runBlocking { delay(300) }
        File("/tmp/testLog4.log").appendText("Line2\n")
        runBlocking { delay(300) }
        consumedLines shouldBe listOf("Line1", "Line2")
    }


    "Log file should exist in the specified directory" {
        val log = Log("/tmp/testLog5.log") {}
        log.file.parentFile.absolutePath shouldBe "/tmp"
    }

    "Log file should have the specified name" {
        val log = Log("/tmp/testLog6.log") {}
        log.file.name shouldBe "testLog6.log"
    }

    "Closing log should cancel the scope" {
        val log = Log("/tmp/testLog7.log") {}
        log.close()
        log.scope.isActive shouldBe false
    }

    "Log file should not be a directory" {
        val log = Log("/tmp/testLog8.log") {}
        log.file.isDirectory shouldBe false
    }

    "Log file should be writable" {
        val log = Log("/tmp/testLog9.log") {}
        log.file.canWrite() shouldBe true
    }

    "Log file should not be hidden" {
        val log = Log("/tmp/testLog10.log") {}
        log.file.isHidden shouldBe false
    }
})