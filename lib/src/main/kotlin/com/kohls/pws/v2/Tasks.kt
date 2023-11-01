package com.kohls.pws.v2

interface Task {
    fun initialize(): Unit = TODO("Not yet implemented")
    fun perform(): Boolean = TODO("Not yet implemented")

    val validations: List<Validation> // TODO:  Make immutable
}

interface TaskBuilder {
    fun build(): Task
}