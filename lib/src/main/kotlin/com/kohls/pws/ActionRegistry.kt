package com.kohls.pws

import kotlin.reflect.KClass

object ActionRegistry {
    private val registry = mutableMapOf<KClass<*>, (String) -> Action>()

    fun <T : Action> register(actionType: KClass<T>, factory: (String) -> T) = registry.put(actionType, factory)

    fun <T : Action> unregister(actionType: KClass<T>) = registry.remove(actionType)

    fun create(actionType: KClass<*>, name: String): Action {
        val factory = registry[actionType] ?: throw IllegalArgumentException("No factory registered for action type: $actionType")
        return factory(name)
    }
}