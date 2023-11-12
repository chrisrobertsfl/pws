package com.kohls.pws


data class NoOp(override val name: String = generateName()) : Action, ActionConfiguration<NoOp> {
    override fun perform(parameters: Parameters): Parameters {
        TODO("Not yet implemented")
    }

    companion object {
        fun configuration(name: String): ActionConfiguration<NoOp> = NoOp(name)
    }

    override fun configure(): NoOp {
        return NoOp(name)
    }
}