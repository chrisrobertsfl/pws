package com.kohls.pws.dsl

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe

class ProjectSetTest :  FeatureSpec({

    feature("Creation") {
        scenario("Simple") {
            projectSet("simple") {} shouldBe ProjectSet(ProjectSetName("simple"))
        }
    }
})

fun projectSet(name : String, block : ProjectSetBuilder.() -> Unit) : ProjectSet {
    val builder = ProjectSetBuilder(name)
    return builder.apply(block).build()
}

class ProjectSetBuilder(val name : String) {
    fun build() : ProjectSet {
        return ProjectSet(ProjectSetName(name))
    }
}

data class ProjectSet(val name : ProjectSetName)
data class ProjectSetName(val contents : String)