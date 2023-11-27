package com.kohls.pws.model

import com.kohls.base.Directory
import com.kohls.pws.BashScript
import com.kohls.pws.Body
import com.kohls.pws.Parameters

data class GitCheckoutAction(override val name: ActionName, val branch: GitBranch, val target: Directory? = null, val bashScript: BashScript = BASH_SCRIPT) : Action {
    override fun perform(): String {
        TODO("Not yet implemented")
    }

    override fun perform(input: Parameters): Parameters {
        val workingDirectory = target ?: input.getOrThrow("target")
        workingDirectory.existsOrThrow("Invalid targetDirectoryPath: ${workingDirectory.path} does not exist.")
        bashScript.createExecutableScript(workingDirectory = workingDirectory).execute(listOf(branch.name))
        return Parameters.create("target" to workingDirectory)
    }

    companion object {
        // TODO:  Arguments should be modelled
        private val BASH_SCRIPT = BashScript(commandName = "git-checkout", body = Body.fromResource("/bash-scripts/git-checkout.sh"))
    }
}