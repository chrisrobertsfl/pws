package com.kohls.pws


interface Action {
    val name: String
    fun perform(): Map<String, Any>  = TODO("Need to implement")

    companion object {
        val NO_FIELDS : Map<String, Any> = emptyMap()
    }
}