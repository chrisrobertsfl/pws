package com.kohls.pws.model

import com.kohls.pws.Parameters

interface Action {
    val name: ActionName
    fun perform(): String
    fun perform(input : Parameters) : Parameters {
        TODO("Implement please")
    }
}