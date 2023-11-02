package com.kohls.pws.v2

interface Validation {
    fun validate(args: Map<String, Any> = mapOf()): Boolean = false
}