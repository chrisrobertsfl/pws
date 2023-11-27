package com.kohls.pws.dsl

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe

class ProjectSetTest :  FeatureSpec({

    feature("Creation") {
        scenario("Simple") {
            projectSet("simple") {
                project("project1") {}
            } shouldBe ProjectSet(ProjectSetName("simple"), setOf(Project(ProjectName("project1"))))
        }
    }
})

fun projectSet(name : String, block : ProjectSetBuilder.() -> Unit) : ProjectSet {
    val builder = ProjectSetBuilder(name)
    return builder.apply(block).build()
}



class ProjectBuilder(val name : String) {
    fun build() : Project {
        return Project(ProjectName(name))
    }
}

class ProjectSetBuilder(val name : String) {
    val projects = mutableSetOf<Project>()
    fun build() : ProjectSet {
        return ProjectSet(ProjectSetName(name), projects)
    }

    fun project(name : String, block : ProjectBuilder.() -> Unit)  {
        val builder = ProjectBuilder(name)
        projects += builder.apply(block).build()
    }
}

data class ProjectSet(val name : ProjectSetName, val projects : Set<Project>)
data class ProjectSetName(val contents : String)
data class Project(val name : ProjectName)
data class ProjectName(val contents : String)