package com.kohls.pws

import com.ingenifi.engine.ClasspathResource
import com.kohls.pws.tasks.ConfirmationException

data class Project(
    override val id: String, val name: String, val source: Source, val tasks: List<Task> = listOf(), val parallel: Boolean = false, val dependencies: List<String> = listOf(),
) : Entity<Project> {

    override fun compile(lookupTable: LookupTable): Project = this.copy(tasks = tasks.map { it.compile(lookupTable) })

    override fun confirm(): Project {
        val errors = ConfirmationEngine(ruleResources = listOf(ClasspathResource("rules/project.drl"))).run<Project, ConfirmationException.Error>(this)
        if (errors.isNotEmpty()) throw ConfirmationException(message = "Confirmation failed for project $id", errors = errors)
        return this
    }
}

class ProjectBuilder(var idGenerator: IdGenerator = IdGenerator.Universal("project")) {
    lateinit var name: String
    lateinit var source: Source
    val tasks: MutableList<Task> = mutableListOf()
    var parallel: Boolean = false
    val dependencies: MutableList<String> = mutableListOf()

    fun build() = Project(name = name, source = source, tasks = tasks, parallel = parallel, dependencies = dependencies, id = idGenerator.generate())

    fun gitSource(url: String, branch: String, path: String): Unit {
        source = GitSource(url = url, branch = branch, path = Directory(path))
    }

    fun localSource(path: String) {
        this.source = LocalSource(Directory(path))
    }

    inline fun <reified T : TaskBuilder> task(idGenerator: IdGenerator, noinline block: T.() -> Unit) {
        val constructor = T::class.java.getDeclaredConstructor(IdGenerator::class.java)
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
