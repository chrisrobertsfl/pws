package com.kohls.pws.dsl

import kotlin.reflect.KClass

object ActionRegistry {
     private val registry: MutableMap<KClass<out ActionBuilder<*>>, (String) -> ActionBuilder<*>> = mutableMapOf()
    fun <T : ActionBuilder<*>> create(builderType: KClass<T>, name: String): T {
        val builderFactory = registry[builderType] ?: throw IllegalStateException("No builder registered for type: $builderType")
        return builderFactory(name) as T
    }

    fun <T : ActionBuilder<*>> register(builderType: KClass<T>, builderCreator: (String) -> T) {
        registry[builderType] = builderCreator
    }

    fun unregisterAll() {
        registry.clear()
    }
}