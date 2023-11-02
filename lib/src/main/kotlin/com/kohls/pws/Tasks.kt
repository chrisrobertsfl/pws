package com.kohls.pws

interface Task : Entity<Task> {
    fun initialize(): Unit = TODO("Not yet implemented")
    fun perform(): Boolean = TODO("Not yet implemented")

    val validations: List<Validation> // TODO:  Make immutable
}

interface TaskBuilder {
    val idGenerator: IdGenerator
    fun build(): Task
}