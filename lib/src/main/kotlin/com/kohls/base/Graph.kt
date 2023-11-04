package com.kohls.base

import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph

class DependencyGraph(private val adjacencyMap: Map<String, List<String>>) {
    private fun createGuavaGraph(): MutableGraph<String> {
        val graph: MutableGraph<String> = GraphBuilder.directed().allowsSelfLoops(true).build()
        adjacencyMap.forEach { (node, neighbors) ->
            graph.addNode(node)
            neighbors.forEach { neighbor ->
                graph.addNode(neighbor) // Ensure all nodes are added, even if they're only present as neighbors
                graph.putEdge(node, neighbor)
            }
        }
        return graph
    }

    private fun findCycleInGraph(graph: MutableGraph<String>): Set<String> {
        val visited = mutableSetOf<String>()
        val recStack = mutableSetOf<String>()
        val cycle = mutableSetOf<String>()

        fun dfs(node: String): Boolean {
            if (node in recStack) {
                cycle.addAll(recStack.dropWhile { it != node })
                return true
            }
            if (!visited.add(node)) return false

            recStack.add(node)
            graph.successors(node)?.forEach { neighbor ->
                if (dfs(neighbor)) return true
            }
            recStack.remove(node)
            return false
        }

        graph.nodes().forEach { node ->
            if (!visited.contains(node) && dfs(node)) return cycle
        }
        return emptySet()
    }

    fun findCycle(): Set<String> {
        val graph = createGuavaGraph()
        return findCycleInGraph(graph)
    }

    fun findSinks(): Set<String> {
        val allNodes = adjacencyMap.values.flatten().toSet()
        val definedNodes = adjacencyMap.keys
        return allNodes.minus(definedNodes)
    }
}
