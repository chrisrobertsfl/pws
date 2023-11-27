package com.kohls.pws.dsl

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe

class ProjectSetTest :  FeatureSpec({

    feature("Creation") {
        projectSet("simple") shouldBe ProjectSet( ProjectSetName("simple"))
    }
})

fun projectSet(name : String) : ProjectSet {
     return ProjectSet(ProjectSetName(name))
}

data class ProjectSet(val name : ProjectSetName)
data class ProjectSetName(val contents : String)