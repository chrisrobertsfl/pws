package com.kohls.pws.v2

import io.kotest.core.spec.style.StringSpec
import java.util.*

class ProjectCompilerTest : StringSpec({

    val original = Project(name = "name", source = LocalSource("/project"), tasks = emptyList(), parallel = true, dependencies = emptyList())
    val lookupTable = LookupTable()
    "Register project with compiler" {
        val compiler = ProjectCompiler(lookupTable)
        compiler.compile(original)
    }
})

sealed class Compiler<T : Any>(private val lookupTable: LookupTable) {
    lateinit var id: String
    fun compile(target: T): T {
        id = lookupTable.addEntry(target)
        println("id = ${id}")
        return doCompilation(target)
    }

    abstract fun doCompilation(target: T): T
}

data class ProjectCompiler(val lookupTable: LookupTable) : Compiler<Project>(lookupTable = lookupTable) {
    override fun doCompilation(target: Project): Project {
        println("Hello I am compiler $id and I'm happily compiling: ${target}")
        return target
    }

}

data class LookupTable(val projectEntries: MutableMap<String, ProjectEntry> = mutableMapOf()) {
    fun findProjectSourcePath(): String {
        return "/tmp"
    }

    fun findById(id: String): Any? {
        return projectEntries[id]
    }

    fun addEntry(registrant: Any): String {
        val id = UUID.randomUUID().toString()
        when (registrant) {
            is Project -> projectEntries[id] = ProjectEntry(registrant)
        }
        return id

    }
}

data class ProjectEntry(val project: Project)