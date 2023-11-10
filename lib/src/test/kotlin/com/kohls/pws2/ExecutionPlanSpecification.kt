package com.kohls.pws2

import com.kohls.base.DependencyGraph
import com.kohls.base.TraversalType
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import kotlin.test.Ignore

@Ignored
class ExecutionPlanSpecification : FeatureSpec({

    feature("Execution Plan Creation:") {
        scenario("By Execution Planner") {
            val workspace = Workspace(
                "workspace", projects = mapOf(
                    "D" to Project("D", dependencies = setOf(ProjectDependency("C"))), "B" to Project("B", dependencies = setOf(ProjectDependency("A"))), "A" to Project(
                        "A", actions = mapOf(
                            "A2" to NoOp("A2", setOf(ActionDependency("A1"))),
                            "A1" to NoOp("A1"),
                            "A3" to NoOp("A3"),
                        )
                    ), "C" to Project("C", dependencies = setOf(ProjectDependency("B")))
                )
            )
            ExecutionPlanner().plan(workspace) shouldBe ExecutionPlan(
                mapOf(
                    1 to ProjectItem(1, "A", mapOf(1 to ActionItem(1, "A1"), 2 to ActionItem(2, "A2"))), 2 to ProjectItem(2, "B"), 3 to ProjectItem(3, "C"), 4 to ProjectItem(4, "D")
                )
            )
        }
    }
})

class ExecutionPlanner {
    fun plan(workspace: Workspace): ExecutionPlan {
        val projectItems = planProjectItems(workspace.projects)
        return ExecutionPlan(projectItems)
    }

    private fun planProjectItems(projects: Map<String, Project>): Map<Int, ProjectItem> {
        var names: MutableList<String> = mutableListOf()
        val adjacencyMap = projects.map { it.key to it.value.dependencies.map { dependency -> dependency.name }.toList() }.toMap()
        val graph = DependencyGraph(adjacencyMap)
        graph.dfsVisit(graph.findMostDependentNode(), TraversalType.POST_ORDER) { names += it }
        var counter: Int = 1
        names = if (names.size == 1 && names[0].isEmpty()) mutableListOf() else names
        val projectItems = names.map { ProjectItem(counter++, it, planActionItems(projects[it] ?: throw IllegalArgumentException("No project found for name '$it'"))) }
        return projectItems.associateBy { it.priority }
    }

    private fun planActionItems(project: Project): Map<Int, ActionItem> {
        var names: MutableList<String> = mutableListOf()
        val adjacencyMap = project.actions.map { it.key to it.value.dependencies.map { dependency -> dependency.name }.toList() }.toMap()
        val graph = DependencyGraph(adjacencyMap)
        graph.dfsVisit(graph.findMostDependentNode(), TraversalType.POST_ORDER) { names += it }
        var counter: Int = 1
        names = if (names.size == 1 && names[0].isEmpty()) mutableListOf() else names
        val projectItems = names.map { ActionItem(counter++, it) }
        return projectItems.associateBy { it.priority }
    }

}

interface ExecutableItem {
    val priority: Int
    val name: String

}

data class WorkItem(override val priority: Int, override val name: String) : ExecutableItem
data class ActionItem(override val priority: Int, override val name: String) : ExecutableItem by WorkItem(priority, name)
data class ProjectItem(override val priority: Int, override val name: String, val actionItems: Map<Int, ActionItem> = emptyMap()) : ExecutableItem by WorkItem(priority, name)
data class ExecutionPlan(val projectItems: Map<Int, ProjectItem>)
