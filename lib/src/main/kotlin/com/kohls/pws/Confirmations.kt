package com.kohls.pws

import com.ingenifi.engine.Engine
import com.ingenifi.engine.Option
import com.ingenifi.engine.Option.SHOW_FACTS
import com.ingenifi.engine.Option.TRACK_RULES
import com.ingenifi.engine.RuleResource


data class ConfirmationEngine(val ruleResources: List<RuleResource> = emptyList(), val options: List<Option> = listOf(TRACK_RULES, SHOW_FACTS)) {
    inline fun <T : Any, reified R> run(vararg input: T): List<R> = run(input.toList())

    inline fun <T : Any, reified R> run(input: List<T>): List<R> {
        return Engine(ruleResources = ruleResources, options = options).executeRules(input).retrieveFacts { it is R }.map { it as R }
    }

}