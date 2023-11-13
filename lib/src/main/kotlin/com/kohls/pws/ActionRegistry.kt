package com.kohls.pws

import kotlin.reflect.KClass


// TODO: Test separately
// TODO: Also is this a best practice to have this as a global?
object ActionRegistry {
    private val registry = mutableMapOf<KClass<*>, (String) -> Action>()

    fun <T : Action> register(actionType: KClass<T>, factory: (String) -> T) = registry.put(actionType, factory)

    fun <T : Action> unregister(actionType: KClass<T>) = registry.remove(actionType)

    fun create(actionType: KClass<*>, name: String): Action {
        val factory = registry[actionType] ?: throw IllegalArgumentException("No factory registered for action type: $actionType")
        return factory(name)
    }
}