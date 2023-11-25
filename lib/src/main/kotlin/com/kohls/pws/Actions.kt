package com.kohls.pws

import com.kohls.pws.Parameters.Companion.EMPTY


interface Action {
    val name: String
    fun perform(parameters: Parameters = EMPTY): Parameters
}

data class Parameters(private var params: MutableMap<String, Any> = mutableMapOf()) {
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T? = params[key] as? T

    fun <T> getOrThrow(key: String): T = get<T>(key) ?: throw IllegalArgumentException("Missing $key")

    operator fun <T> set(key: String, value: T) {
        params[key] = value as Any
    }

    operator fun plusAssign(pair: Pair<String, Any>) {
        this[pair.first] = pair.second
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


