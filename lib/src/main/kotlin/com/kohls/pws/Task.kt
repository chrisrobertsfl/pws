package com.kohls.pws

interface Task {
    fun initialize(): Unit
    fun perform(): Boolean = false

    var validations: List<Validation>
}