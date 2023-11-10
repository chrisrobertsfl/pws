package com.kohls.pws2

import com.ingenifi.engine.Engine
import com.ingenifi.engine.Option
import com.ingenifi.engine.StringResource
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe

class ExecutionOrderSpecification : FeatureSpec({

    feature("Execution Order between Projects") {

        scenario("One project should have that project as its execution order") {
            executionOrder(Project("A")) shouldBe ExecutionOrder("A")
        }

        scenario("Project where one depends on its dependencies should have separate steps with dependencies going first") {
            executionOrder(Project("A", dependencies = setOf(ProjectDependency("B"), ProjectDependency("C")))) shouldBe ExecutionOrder("B", "C", "A")

        }
    }
})


val rules = """
            package com.kohls.pws2;
            import java.util.List;
            import com.kohls.pws2.ExecutionOrder;
            import com.kohls.pws2.ExecutionStep;

            rule "Create initial execution order if there are any execution steps there"
            when
                exists ExecutionStep()
                not ExecutionOrder()
            then
                insert(new ExecutionOrder());
             end
             
            rule "Add Step to Order"
            when
                step: ExecutionStep()
                order: ExecutionOrder()
            then
                modify(order) { addStep(step); }
                retract(step);
             end
             
             rule "Project should introduce its dependencies"
             when
                project: Project()
                dependency: ProjectDependency( dependencyName:  name ) from project.dependencies
                not ExecutionStep( name == dependencyName )
             then
                insert(dependency);
              end
                
        """.trimIndent()

private fun executionOrder(vararg facts: Any) =
    Engine(ruleResources = listOf(StringResource(rules)), options = listOf(Option.TRACK_RULES, Option.SHOW_FACTS)).executeRules(facts.toList()).retrieveFacts { it is ExecutionOrder }
        .map { it as ExecutionOrder }.first()

data class ExecutionStep(val name: String)
data class ExecutionOrder(val steps:  MutableList<ExecutionStep> = mutableListOf()) {
    constructor(vararg names : String) : this(names.map { ExecutionStep(it) }.toMutableList())

    fun addStep(step : ExecutionStep) {
        steps += step
    }

}