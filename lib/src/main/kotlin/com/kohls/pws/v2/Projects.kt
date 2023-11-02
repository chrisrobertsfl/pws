package com.kohls.pws.v2

data class Project(
    override val id: String, val name: String, val source: Source, val tasks: List<Task> = listOf(), val parallel: Boolean = false, val dependencies: List<String> = listOf(),
) : Entity<Project> {
    fun getSourcePath() = when (source) {
        is LocalSource -> source.path
        is GitSource -> source.directory
        else -> throw IllegalArgumentException("Need to deal with unknown source")
    }

    override fun compile(lookupTable: LookupTable): Project {
        return this.copy(tasks = tasks.map{ it.compile(lookupTable)})
    }
}

class ProjectBuilder(var idGenerator: IdGenerator = IdGenerator.Universal("project")) {
    lateinit var name: String
    lateinit var source: Source
    val tasks: MutableList<Task> = mutableListOf()
    var parallel: Boolean = false
    val dependencies: MutableList<String> = mutableListOf()

    fun build(): Project {
        return Project(
            name = name, source = source, tasks = tasks, parallel = parallel, dependencies = dependencies, id = idGenerator.generate()
        )
    }

    fun gitSource(url: String, branch: String, directory: String): Unit {
        source = GitSource(url, branch, directory)
    }

    fun localSource(path: String) {
        this.source = LocalSource(path)
    }

    inline fun <reified T : TaskBuilder> task(
        idGenerator: IdGenerator,
        noinline block: T.() -> Unit
    ) {
        // Find the constructor that takes an IdGenerator as the only argument
        val constructor = T::class.java.getDeclaredConstructor(IdGenerator::class.java)
        // Create a new instance of T using the constructor with idGenerator
        val taskBuilder = constructor.newInstance(idGenerator)
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
