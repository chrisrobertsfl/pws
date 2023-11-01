package com.kohls.pws.v2

data class Project(
    val name: String, val source: Source?, val tasks: List<Task> = listOf(), val parallel: Boolean = false, val dependencies: List<String> = listOf()
) {
    fun getSourcePath() = when (source) {
        is LocalSource -> source.path
        is GitSource -> source.directory
        else -> throw IllegalArgumentException("Need to deal with unknown source")
    }
}

class ProjectBuilder {
    lateinit var name: String
    var source: Source? = null
    val tasks: MutableList<Task> = mutableListOf()
    var parallel: Boolean = false
    val dependencies: MutableList<String> = mutableListOf()

    fun build(): Project {
        return Project(
            name = name, source = source, tasks = tasks, parallel = parallel, dependencies = dependencies
        )
    }

    fun gitSource(url: String, branch: String, directory: String): Unit {
        source = GitSource(url, branch, directory)
    }

    fun localSource(path: String) {
        this.source = LocalSource(path)
    }

    inline fun <reified T : TaskBuilder> task(noinline block: T.() -> Unit) {
        val taskBuilder = T::class.java.getDeclaredConstructor().newInstance()
        taskBuilder.block()
        tasks += taskBuilder.build()
    }


    fun dependencies(builder: DependenciesBuilder.() -> Unit) {
        val dependenciesBuilder = DependenciesBuilder().apply(builder)
        this.dependencies.addAll(dependenciesBuilder.dependencies)
    }
}

class DependenciesBuilder {
    val dependencies: MutableList<String> = mutableListOf()

    fun executesAfter(name: String) {
        dependencies.add(name)
    }
}
