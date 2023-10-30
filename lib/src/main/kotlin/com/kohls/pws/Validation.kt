package com.kohls.pws

interface Validation {

    fun validate(args : Map<String, Any> = mapOf()) : Boolean = false

}