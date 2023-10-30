package com.kohls.pws

import com.kohls.pws.Source.UNKNOWN

class ProjectBuilder {
    lateinit var name: String
    private val executesAfterList: MutableList<String> = mutableListOf()
    var source: Source = UNKNOWN
    var canBeParallelized: Boolean = false
    val tasks: MutableList<Task> = mutableListOf()
    fun build(): Project = Project(name = name, executesAfterList = executesAfterList, source = source, canBeParallelized = canBeParallelized, tasks = tasks)
    fun executesAfter(name: String) {
        executesAfterList += name
    }

    inline fun <reified T : Task> task(noinline block: T.() -> Unit) {
        val taskInstance = T::class.java.getDeclaredConstructor().newInstance()
        taskInstance.block()
        tasks += taskInstance
    }

}