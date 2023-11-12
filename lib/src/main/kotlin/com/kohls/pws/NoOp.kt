package com.kohls.pws

import com.kohls.pws.Action.Companion.generateName

data class NoOp(override val name: String = generateName()) : Action {
    override fun perform(parameters: Parameters): Parameters {
        TODO("Not yet implemented")
    }
}