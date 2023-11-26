package com.kohls.pws

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ActionRegistryTest : FeatureSpec({

    feature("Registration") {
        scenario("Successfully register an action type") {
            ActionRegistry.register(DummyActionA::class) { name -> DummyActionA(name) }
            val action = ActionRegistry.create(DummyActionA::class, "TestActionA")
            action shouldNotBe null
            action.name shouldBe "TestActionA"
        }

        scenario("Register multiple action types") {
            ActionRegistry.register(DummyActionA::class) { name -> DummyActionA(name) }
            ActionRegistry.register(DummyActionB::class) { name -> DummyActionB(name) }
            val actionA = ActionRegistry.create(DummyActionA::class, "TestActionA")
            val actionB = ActionRegistry.create(DummyActionB::class, "TestActionB")
            actionA.name shouldBe "TestActionA"
            actionB.name shouldBe "TestActionB"
        }

        scenario("Re-register an action type") {
            ActionRegistry.register(DummyActionA::class) { name -> DummyActionA(name) }
            ActionRegistry.register(DummyActionA::class) { name -> DummyActionA("New$name") }
            val action = ActionRegistry.create(DummyActionA::class, "Action")
            action.name shouldBe "NewAction"
        }
    }

    feature("Unregistration") {
        scenario("Successfully unregister an action type") {
            ActionRegistry.register(DummyActionA::class) { name -> DummyActionA(name) }
            ActionRegistry.unregister(DummyActionA::class)
            val act = runCatching { ActionRegistry.create(DummyActionA::class, "TestActionA") }
            act.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        }

        scenario("Unregister a non-registered action type") {
            val act = runCatching { ActionRegistry.unregister(DummyActionB::class) }
            act.isSuccess shouldBe true
        }

        scenario("Unregister and then register the same action type") {
            ActionRegistry.register(DummyActionA::class) { name -> DummyActionA(name) }
            ActionRegistry.unregister(DummyActionA::class)
            ActionRegistry.register(DummyActionA::class) { name -> DummyActionA(name) }
            val action = ActionRegistry.create(DummyActionA::class, "TestActionA")
            action.name shouldBe "TestActionA"
        }
    }

    feature("Creation") {
        scenario("Create an action after registration") {
            ActionRegistry.register(DummyActionA::class) { name -> DummyActionA(name) }
            val action = ActionRegistry.create(DummyActionA::class, "TestActionA")
            action.name shouldBe "TestActionA"
        }

        scenario("Attempt to create an action without registration") {
            val act = runCatching { ActionRegistry.create(DummyActionB::class, "TestActionB") }
            act.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        }

        scenario("Create multiple actions of the same type") {
            ActionRegistry.register(DummyActionA::class) { name -> DummyActionA(name) }
            val action1 = ActionRegistry.create(DummyActionA::class, "Action1")
            val action2 = ActionRegistry.create(DummyActionA::class, "Action2")
            action1.name shouldBe "Action1"
            action2.name shouldBe "Action2"
        }
    }
})


class DummyActionA(override val name: String) : Action {
    override fun perform(parameters: Parameters): Parameters = Parameters.create("result" to "Performed $name")
}

class DummyActionB(override val name: String) : Action {
    override fun perform(parameters: Parameters): Parameters =Parameters.create("result" to "Performed $name")
}
