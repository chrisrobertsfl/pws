package com.kohls.pws.v2

import java.util.*

interface IdGenerator {
    fun generate(): String

    class Universal(var prefix: String? = null) : IdGenerator {
        override fun generate(): String = buildString {
            prefix?.let { append("${prefix}-") }
            append(UUID.randomUUID().toString())
        }
    }
}

