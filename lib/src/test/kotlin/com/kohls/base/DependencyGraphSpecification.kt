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

    feature("DFS Visits:") {
        scenario("Pre-order traversal should visit nodes before their descendants") {
            val graphData = mapOf("a" to listOf("b", "c"), "b" to listOf("d"), "c" to listOf("e"))
            val graph = DependencyGraph(graphData)
            val visitOrder = mutableListOf<String>()

            graph.dfsVisit("a", TraversalType.PRE_ORDER) { visitOrder.add(it) }

            val expectedOrder = listOf("a", "b", "d", "c", "e")
            visitOrder shouldBe expectedOrder
        }
        scenario("Post-order traversal should visit nodes after their descendants") {
            val graphData = mapOf("a" to listOf("b", "c"), "b" to listOf("d"), "c" to listOf("e"))
            val graph = DependencyGraph(graphData)
            val visitOrder = mutableListOf<String>()

            graph.dfsVisit("a", TraversalType.POST_ORDER) { visitOrder.add(it) }

            val expectedOrder = listOf("d", "b", "e", "c", "a")
            visitOrder shouldBe expectedOrder
        }

        scenario("Reverse post order traversal") {
            val graphData = mapOf("a" to listOf("b", "c"), "b" to listOf("d"), "c" to listOf("e"))
            val graph = DependencyGraph(graphData)
            val visitOrder = mutableListOf<String>()

            // Start the DFS from 'a'
            graph.dfsVisit("a", TraversalType.REVERSE_POST_ORDER) { node ->
                // This will capture the reverse post-order traversal of the DFS
                visitOrder.add(node)
            }

            // The expected order should be the reverse of a post-order traversal.
            val expectedOrder = listOf("a", "c", "e", "b", "d")
            visitOrder shouldBe expectedOrder
        }

        scenario("In-order traversal") {
            val graphData = mapOf(
                "a" to listOf("b", "c"),
                "b" to listOf("d", "e"),
                "c" to listOf("f", "g"),
                "d" to listOf("h"),
                "e" to listOf(), // Explicitly declare empty children lists for leaves if necessary
                "f" to listOf(),
                "g" to listOf("i", "j"),
                "h" to listOf(),
                "i" to listOf(),
                "j" to listOf()
            )
            val graph = DependencyGraph(graphData)
            val visitOrder = mutableListOf<String>()

            graph.dfsVisit("a", TraversalType.IN_ORDER) { node ->
                visitOrder.add(node)
            }

            val expectedOrder = listOf("h", "d", "b", "e", "a", "f", "c", "i", "g", "j")
            visitOrder shouldBe expectedOrder
        }

    }
})
