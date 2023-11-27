package com.kohls.pws.dsl

import com.kohls.pws.model.*
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ActionRegistryTest : FeatureSpec({

    beforeTest {
        ActionRegistry.unregisterAll()
    }
    feature("Registration with Creation") {
        scenario("Successfully register and create dummy action builder type") {
            ActionRegistry.register(DummyBuilder::class) { name -> DummyBuilder(name) }
            val builder: ActionBuilder<DummyAction> = ActionRegistry.create(DummyBuilder::class, "Dummy name").also {
                it.name shouldNotBe null
                it.name shouldBe "Dummy name"
            }
            builder.build().also {
                it.name shouldBe ActionName(builder.name)
                it.perform() shouldBe "DummyAct performing"
            }
        }

        scenario("Fail to register and create dummy action builder type") {
            shouldThrowExactly<IllegalStateException> { ActionRegistry.create(DummyBuilder::class, "Dummy name") }.message shouldBe "No builder registered for type: class com.kohls.pws.dsl.DummyBuilder"
        }

        scenario("Successfully register dummy1 action builder type") {
            ActionRegistry.register(Dummy1Builder::class) { name -> Dummy1Builder(name) }
            val builder: ActionBuilder<Dummy1Action> = ActionRegistry.create(Dummy1Builder::class, "Dummy1 name").also {
                it.name shouldNotBe null
                it.name shouldBe "Dummy1 name"
            }
            builder.build().also {
                it.name shouldBe ActionName(builder.name)
                it.perform() shouldBe "Dummy1Act performing"
            }
        }
    }
})


class DummyBuilder(override val name: String) : ActionBuilder<DummyAction> {
    override fun build(): DummyAction {
        return DummyAction(ActionName(name))
    }
}

data class DummyAction(override val name: ActionName) : Action {
    override fun perform(): String {
        return "DummyAct performing"
    }
}

class Dummy1Action(override val name: ActionName) : Action {

    override fun perform(): String {
        return "Dummy1Act performing"
    }
}

class Dummy1Builder(override val name: String) : ActionBuilder<Dummy1Action> {
    override fun build(): Dummy1Action {
        return Dummy1Action(ActionName(name))
    }
}

