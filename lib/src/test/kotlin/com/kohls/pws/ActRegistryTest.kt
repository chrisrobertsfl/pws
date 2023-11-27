package com.kohls.pws

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.reflect.KClass


class ActRegistryTest : FeatureSpec({

    beforeTest {
        ActRegistry.unregisterAll()
    }
    feature("Registration with Creation") {
        scenario("Successfully register and create dummy action builder type") {
            ActRegistry.register(DummyBuilder::class) { name -> DummyBuilder(name) }
            val builder: ActBuilder<DummyAct> = ActRegistry.create(DummyBuilder::class, "Dummy name").also {
                it.name shouldNotBe null
                it.name shouldBe "Dummy name"
            }
            builder.build().also {
                it.name shouldBe builder.name
                it.perform() shouldBe "DummyAct performing"
            }
        }

        scenario("Fail to register and create dummy action builder type") {
            shouldThrowExactly<IllegalStateException> { ActRegistry.create(DummyBuilder::class, "Dummy name") }.message shouldBe "No builder registered for type: class com.kohls.pws.DummyBuilder"
        }

        scenario("Successfully register dummy1 action builder type") {
            ActRegistry.register(Dummy1Builder::class) { name -> Dummy1Builder(name) }
            val builder: ActBuilder<Dummy1Act> = ActRegistry.create(Dummy1Builder::class, "Dummy1 name").also {
                it.name shouldNotBe null
                it.name shouldBe "Dummy1 name"
            }
            builder.build().also {
                it.name shouldBe builder.name
                it.perform() shouldBe "Dummy1Act performing"
            }
        }
    }
})

class DummyBuilder(override val name: String) : ActBuilder<DummyAct> {
    override fun build(): DummyAct {
        return DummyAct(name)
    }
}

data class DummyAct(override val name: String) : Act {
    override fun perform(): String {
        return "DummyAct performing"
    }
}

class Dummy1Act(override val name: String) : Act {

    override fun perform(): String {
        return "Dummy1Act performing"
    }
}

class Dummy1Builder(override val name: String) : ActBuilder<Dummy1Act> {
    override fun build(): Dummy1Act {
        return Dummy1Act(name)
    }
}

object ActRegistry {
//    private val registry: Map<KClass<out ActBuilder<*>>, (String) -> ActBuilder<*>> =
//        mapOf(DummyBuilder::class to { name -> DummyBuilder(name) }, Dummy1Builder::class to { name -> Dummy1Builder(name) })


    private val registry: MutableMap<KClass<out ActBuilder<*>>, (String) -> ActBuilder<*>> = mutableMapOf()
    fun <T : ActBuilder<*>> create(builderType: KClass<T>, name: String): T {
        val builderFactory = registry[builderType] ?: throw IllegalStateException("No builder registered for type: $builderType")
        return builderFactory(name) as T
    }

    fun <T : ActBuilder<*>> register(builderType: KClass<T>, builderCreator: (String) -> T) {
        registry[builderType] = builderCreator
    }

    fun unregisterAll() {
        registry.clear()
    }
}

interface ActBuilder<T : Act> {
    val name: String
    fun build(): T
}

interface Act {
    val name: String
    fun perform(): String
}