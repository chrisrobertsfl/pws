package com.kohls.pws.v2

import java.util.*

interface Entity<T> : IsCompilable<T>, HasIdentity

interface HasIdentity {
    val id: String
}

interface IsCompilable<T> {
    fun compile(lookupTable: LookupTable): T
}

interface IdGenerator {
    fun generate(): String

    class Universal(var prefix: String? = null) : IdGenerator {
        override fun generate(): String = buildString {
            prefix?.let { append("${prefix}-") }
            append(UUID.randomUUID().toString())
        }
    }
}
