package com.kohls.pws.dsl

import com.kohls.pws.model.Action

interface ActionBuilder<T : Action> {
    val name: String
    fun build(): T
}