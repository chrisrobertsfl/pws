package com.kohls.pws

import com.kohls.base.Eventually
import kotlin.time.Duration

data class FileEventuallyContains(override val name: String = Action.generateName(), val duration: Duration, val searchedText : String ) : Action {
    override fun perform(parameters: Parameters): Parameters {
        val logFile =parameters.getOrThrow<LogFile>("logFile")

        Eventually(condition = {
            logFile.validate { it.readText().contains(searchedText) }
        }).satisfiedWithinOrThrow(duration = duration, message = "Could not find '${searchedText}' in ${logFile.fullPath()} within $duration")
     return Parameters.EMPTY
    }
}