package com.kohls.pws.v2

import java.io.File

data class Directory(val path: String) {
    private val directoryPath: File = File(path)
    fun delete() = directoryPath.deleteRecursively()
    fun create() = directoryPath.mkdirs()
    private fun exists(): Boolean = directoryPath.exists()
    fun canBeCreated() = !exists() && directoryPath.parentFile?.canWrite() ?: false
    fun isNotThere(): Boolean = path.isBlank()
}