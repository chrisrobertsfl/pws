package com.kohls.base

import java.io.File
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

data class Directory(private val inputPath: String) {
    val path = Paths.get(inputPath)

    @OptIn(ExperimentalPathApi::class)
    fun delete(): Directory = apply {
        path.deleteRecursively()
    }

    fun exists(): Boolean = path.exists()


    fun existsOrThrow(message: String = "Directory path $inputPath does not exist"): Directory {
        require(exists()) { message }
        return this
    }

    fun toFile(): File = path.toFile()
}
