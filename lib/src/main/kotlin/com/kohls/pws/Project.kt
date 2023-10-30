package com.kohls.pws

import com.kohls.pws.Source.UNKNOWN

data class Project(
    val name: String, val executesAfterList: List<String> = listOf(), val source: Source = UNKNOWN, val tasks: List<Task> = listOf(), val canBeParallelized: Boolean = false
)