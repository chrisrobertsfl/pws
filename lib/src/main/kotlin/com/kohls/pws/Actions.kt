package com.kohls.pws

import com.kohls.pws.Parameters.Companion.EMPTY
import kotlin.reflect.KClass


interface Action {
    val name: String
    fun perform(parameters: Parameters = EMPTY): Parameters
}

fun interface ActionConfiguration<T : Action> {
    fun configure(): T
}

object ActionRegistry {
     val registry = mutableMapOf<KClass<*>, (String) -> ActionConfiguration<*>>()

    fun <T : Action, C : ActionConfiguration<T>> register(actionClass: KClass<T>, configCreator: (String) -> C) {
        registry[actionClass] = configCreator
    }

    inline fun <reified T : Action, reified C : ActionConfiguration<T>> create(name: String, noinline configure: C.() -> Unit): T {
        val configFactory = registry[T::class] as? (String) -> C
            ?: throw IllegalArgumentException("No configuration factory registered for action type: ${T::class.simpleName}")

        val config = configFactory(name)
        configure(config)
        return config.configure()
    }
}





data class Parameters(private val params: MutableMap<String, Any>) {
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T? = params[key] as? T

    fun <T> getOrThrow(key: String): T = get<T>(key) ?: throw IllegalArgumentException("Missing $key")

    operator fun <T> set(key: String, value: T) {
        params[key] = value as Any
    }

    companion object {
        fun create(vararg pairs: Pair<String, Any>): Parameters {
            val map = mutableMapOf<String, Any>()
            pairs.forEach { map[it.first] = it.second }
            return Parameters(map)
        }

        val EMPTY = create()
    }
}


