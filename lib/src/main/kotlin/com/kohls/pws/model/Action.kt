package com.kohls.pws.model

interface Action {
    val name: ActionName
    fun perform(): String
}