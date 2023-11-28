package com.kohls.pws.model

import com.kohls.base.Directory
import com.kohls.pws.BashScript
import com.kohls.pws.Parameters

data class GitCloneAction(override val name: ActionName, val repositoryUrl: GitRepositoryUrl, private var target: Directory, val bashScript: BashScript, val overwrite: Boolean) : Action {
    override fun perform(): String {
        TODO("Not yet implemented")
    }

    override fun perform(input: Parameters): Parameters {
        if (target.exists()) {
            when (overwrite) {
                true -> target.delete()
                false -> throw IllegalStateException("target directory '${target.path}' already exists and cannot be overwritten when overwrite is enabled")
            }
        }
        return Parameters.create("target" to target)
    }

}