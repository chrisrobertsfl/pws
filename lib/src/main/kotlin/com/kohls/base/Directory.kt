package com.kohls.base

import java.io.File

data class Directory(val path: String) {
    private val directoryPath: File = File(path)
    fun delete() = directoryPath.deleteRecursively()
    fun create() = directoryPath.mkdirs()
    fun exists(): Boolean = directoryPath.exists()
    fun canBeCreated() = !exists() && directoryPath.parentFile?.canWrite() ?: false
    fun isNotThere(): Boolean = path.isBlank()
    fun append(childPath: String): Directory {
        val childPathNoSlashes = childPath.dropWhile { it == '/' }
        return Directory("$path/$childPathNoSlashes")
    }



    fun asFile(fileName: String): File {
        return directoryPath.resolve(fileName)
    }
}