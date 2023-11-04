package com.kohls.base

import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe

class DependencyGraphSpecification : FeatureSpec({

    feature("Find cycles:") {
        scenario("a->b->c") {
           DependencyGraph(mapOf("a" to listOf("b"), "b" to listOf("c"), "c" to listOf("a"))).findCycle().asClue {
                it shouldBe setOf("a", "b", "c")
            }
        }

        scenario("None in a->b, b->c") {
           DependencyGraph(mapOf("a" to listOf("b"), "b" to listOf("c"))).findCycle().asClue {
                it shouldBe emptySet()
            }
        }

        scenario("Simple map") {
           DependencyGraph(mapOf("A" to listOf("B"), "B" to listOf("C"), "C" to listOf("A"))).findCycle().asClue {
                it shouldBe setOf("A", "B", "C")
            }
        }

        scenario("None in a linear map") {
           DependencyGraph(mapOf("A" to listOf("B"), "B" to listOf("C"), "C" to listOf("D"), "D" to emptyList())).findCycle().asClue {
                it shouldBe emptySet()
            }
        }

        scenario("Map with branches") {
           DependencyGraph(mapOf("A" to listOf("B"), "B" to listOf("C", "D"), "C" to emptyList(), "D" to listOf("E"), "E" to listOf("B"))).findCycle().asClue {
                it shouldBe setOf("B", "D", "E")
            }
        }

        scenario("Complex map") {
            val complexMap = mapOf(
                "A" to listOf("B"), "B" to listOf("C"), "C" to listOf("D", "E"), "D" to listOf("A"), "E" to listOf("F"), "F" to listOf("G", "H"), "G" to listOf("E"), "H" to emptyList()
            )
           DependencyGraph(complexMap).findCycle().asClue {
                it shouldBe setOf("A", "B", "C", "D")
            }
        }

        scenario("None in a self-loop map") {
            val selfLoopMap = mapOf("A" to listOf("A"), "B" to listOf("B", "C"), "C" to listOf("C"))
           DependencyGraph(selfLoopMap).findCycle().asClue {
                it shouldBe setOf("A")
            }
        }
    }


    feature("Find sinks:") {
        scenario("single orphaned dependency") {
            val graph = mapOf("a" to listOf("b"), "b" to listOf("c", "d"), "d" to emptyList())
           DependencyGraph(graph).findSinks().asClue {
                it shouldBe setOf("c")
            }
        }

        scenario("multiple orphaned dependencies") {
            val graph = mapOf("a" to listOf("b", "e"), "b" to listOf("c"), "c" to listOf("f"))
           DependencyGraph(graph).findSinks().asClue {
                it shouldBe setOf("e", "f")
            }
        }

        scenario("no orphaned dependencies") {
            val graph = mapOf("a" to listOf("b"), "b" to listOf("c"), "c" to listOf("a"))
           DependencyGraph(graph).findSinks().asClue {
                it shouldBe emptySet()
            }
        }

        scenario("all nodes are orphaned") {
            val graph = mapOf<String, List<String>>()
           DependencyGraph(graph).findSinks().asClue {
                it shouldBe emptySet()
            }
        }

        scenario("linear dependency with an end orphaned node") {
            val graph = mapOf("a" to listOf("b"), "b" to listOf("c"), "c" to listOf("d"))
           DependencyGraph(graph).findSinks().asClue {
                it shouldBe setOf("d")
            }
        }

        scenario("circular dependency with an orphaned node") {
            val graph = mapOf("a" to listOf("b"), "b" to listOf("a", "e"))
           DependencyGraph(graph).findSinks().asClue {
                it shouldBe setOf("e")
            }
        }

        scenario("self-referential node with orphaned dependency") {
            val graph = mapOf("a" to listOf("a", "b"))
           DependencyGraph(graph).findSinks().asClue {
                it shouldBe setOf("b")
            }
        }

        scenario("complex graph with multiple orphaned dependencies") {
            val complexMap = mapOf(
                "A" to listOf("B"), "B" to listOf("C", "J"), "C" to listOf("D", "E"), "D" to listOf("A"), "E" to listOf("F"), "F" to listOf("G", "H"), "G" to listOf("E", "I")
            )
           DependencyGraph(complexMap).findSinks().asClue {
                it shouldBe setOf("H", "I", "J")
            }
        }

        scenario("orphaned dependency with self-loop") {
            val graph = mapOf("a" to listOf("a", "b"), "b" to listOf("b", "c"), "c" to listOf("d"))
           DependencyGraph(graph).findSinks().asClue {
                it shouldBe setOf("d")
            }
        }

        scenario("disconnected graph with orphaned dependencies") {
            val graph = mapOf("a" to listOf("b"), "c" to listOf("d"), "e" to listOf("f"))
           DependencyGraph(graph).findSinks().asClue {
                it shouldBe setOf("b", "d", "f")
            }
        }
    }
})
