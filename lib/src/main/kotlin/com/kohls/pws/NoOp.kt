package com.kohls.pws

data class NoOp(override val name: String = generateName()) : Action {
    override fun perform(parameters: Parameters): Parameters {
        TODO("Not yet implemented")
    }
}