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
    private var lastVisited = "" // Mutable property for the most dependent node

    private fun createGuavaGraph(): MutableGraph<String> {
        val graph: MutableGraph<String> = GraphBuilder.directed().allowsSelfLoops(true).build()
        adjacencyMap.forEach { (node, neighbors) ->
            graph.addNode(node)
            neighbors.forEach { neighbor ->
                graph.addNode(neighbor)
                graph.putEdge(node, neighbor)
            }
        }
        return graph
    }

    private fun findCycleInGraph(graph: MutableGraph<String>): Set<String> {
        val visited = mutableSetOf<String>()
        val recStack = mutableSetOf<String>()
        val cycle = mutableSetOf<String>()

        graph.nodes().forEach { node ->
            if (!visited.contains(node) && dfsForCycle(node, graph, visited, recStack, cycle)) {
                return cycle
            }
        }
        return emptySet()
    }

    private fun dfsForCycle(node: String, graph: MutableGraph<String>, visited: MutableSet<String>, recStack: MutableSet<String>, cycle: MutableSet<String>): Boolean {
        if (node in recStack) {
            cycle.addAll(recStack.dropWhile { it != node })
            return true
        }
        if (!visited.add(node)) return false

        recStack.add(node)
        graph.successors(node)?.forEach { neighbor ->
            if (dfsForCycle(neighbor, graph, visited, recStack, cycle)) return true
        }
        recStack.remove(node)
        return false
    }

    private fun findSourceNodes(): Set<String> {
        val allNeighbors = adjacencyMap.values.flatten().toSet()
        return adjacencyMap.keys.filter { it !in allNeighbors }.toSet()
    }

    fun findMostDependentNode(): String {
        val visited = mutableSetOf<String>()
        val sourceNodes = findSourceNodes()

        sourceNodes.forEach { node -> dfsForDependentNode(node, visited) }
        return lastVisited
    }

    private fun dfsForDependentNode(node: String, visited: MutableSet<String>) {
        if (node in visited) return
        visited.add(node)
        adjacencyMap[node]?.forEach { dfsForDependentNode(it, visited) }
        lastVisited = node
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

        dfsVisitNode(startNode, traversalType, visit, visited, exitList)

        if (traversalType == TraversalType.REVERSE_POST_ORDER) {
            exitList.asReversed().forEach(visit)
        } else if (traversalType == TraversalType.POST_ORDER) {
            exitList.forEach(visit)
        }
    }

    private fun dfsVisitNode(node: String, traversalType: TraversalType, visit: (String) -> Unit, visited: MutableSet<String>, exitList: MutableList<String>) {
        if (node in visited) return
        visited.add(node)

        val neighbors = adjacencyMap[node] ?: emptyList()

        if (traversalType == TraversalType.IN_ORDER && neighbors.isNotEmpty()) {
            // Visit the first child
            dfsVisitNode(neighbors.first(), traversalType, visit, visited, exitList)
            // Visit the node
            visit(node)
            // Visit the rest of the children
            neighbors.drop(1).forEach { neighbor ->
                dfsVisitNode(neighbor, traversalType, visit, visited, exitList)
            }
        } else {
            if (traversalType == TraversalType.PRE_ORDER) {
                visit(node)
            }
            neighbors.forEach { neighbor ->
                dfsVisitNode(neighbor, traversalType, visit, visited, exitList)
            }
            if (traversalType == TraversalType.POST_ORDER || traversalType == TraversalType.REVERSE_POST_ORDER) {
                exitList.add(node)
            }
            // For IN_ORDER traversal with no children, visit the node here
            if (traversalType == TraversalType.IN_ORDER && neighbors.isEmpty()) {
                visit(node)
            }
        }
    }


    fun findAllGraphs(): List<DependencyGraph> {
        val visited = mutableSetOf<String>()

        return adjacencyMap.keys.filterNot { it in visited }
            .mapNotNull { startNode ->
                bfsForSubgraph(startNode, visited).takeIf { it.isNotEmpty() }?.let { DependencyGraph(it) }
            }
    }

    private fun bfsForSubgraph(startNode: String, visited: MutableSet<String>): Map<String, List<String>> {
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
}
