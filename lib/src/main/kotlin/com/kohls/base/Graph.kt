package com.kohls.base

import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph


enum class TraversalType {
    PRE_ORDER,
    IN_ORDER,
    POST_ORDER,
    REVERSE_POST_ORDER
}

data class DependencyGraph(private val adjacencyMap: Map<String, List<String>>) {
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

    private fun findSourceNodes(): Set<String> {
        val allNeighbors = adjacencyMap.values.flatten().toSet()
        // Source nodes are those present in keys but not as neighbors
        return adjacencyMap.keys.filter { it !in allNeighbors }.toSet()
    }

    fun findMostDependentNode(): String {
        var lastVisited = ""
        val visited = mutableSetOf<String>()
        val sourceNodes = findSourceNodes()

        fun dfs(node: String) {
            if (node in visited) return
            visited.add(node)
            adjacencyMap[node]?.forEach(::dfs)
            lastVisited = node // The last visited node in post-order will be the most dependent one
        }

        sourceNodes.forEach(::dfs)
        return lastVisited
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

    fun dfsVisit(startNode: String, traversalType: TraversalType, visit: (String) -> Unit) {
        val visited = mutableSetOf<String>()
        val exitList = mutableListOf<String>()

        fun dfs(node: String) {
            if (node in visited) return
            visited.add(node)

            val neighbors = adjacencyMap[node]

            if (traversalType == TraversalType.IN_ORDER && neighbors?.isNotEmpty() == true) {
                // Visit the first child
                dfs(neighbors.first())
                // Visit the node after the first child
                visit(node)
                // Visit the rest of the children
                neighbors.drop(1).forEach { neighbor ->
                    dfs(neighbor)
                }
            } else {
                if (traversalType == TraversalType.PRE_ORDER) {
                    visit(node)
                }
                // For other than IN_ORDER traversal, visit children normally
                neighbors?.forEach { neighbor ->
                    dfs(neighbor)
                }
                if (traversalType == TraversalType.POST_ORDER || traversalType == TraversalType.REVERSE_POST_ORDER) {
                    exitList.add(node)
                }
                if (traversalType == TraversalType.IN_ORDER && neighbors.isNullOrEmpty()) {
                    visit(node)
                }
            }
        }

        dfs(startNode)

        if (traversalType == TraversalType.REVERSE_POST_ORDER) {
            exitList.asReversed().forEach(visit)
        } else if (traversalType == TraversalType.POST_ORDER) {
            exitList.forEach(visit)
        }
    }

    fun findAllGraphs(): List<DependencyGraph> {
        val visited = mutableSetOf<String>()

        fun bfs(startNode: String): Map<String, List<String>> {
            val subgraph = mutableMapOf<String, List<String>>()
            val queue = ArrayDeque<String>()
            queue.add(startNode)

            while (queue.isNotEmpty()) {
                val node = queue.removeFirst()
                if (node !in visited) {
                    visited.add(node)
                    val neighbors = adjacencyMap[node] ?: emptyList()
                    subgraph[node] = neighbors
                    queue.addAll(neighbors.filterNot { it in visited })
                }
            }

            return subgraph
        }

        return adjacencyMap.keys.filterNot { it in visited }
            .mapNotNull { startNode ->
                bfs(startNode).takeIf { it.isNotEmpty() }?.let { DependencyGraph(it) }
            }
    }

}
