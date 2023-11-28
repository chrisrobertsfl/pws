package com.kohls.pws.dsl

import com.kohls.base.Directory
import com.kohls.pws.model.*

class GitClone(override val name : String) : ActionBuilder<GitCloneAction> {
    private var repositoryUrl : GitRepositoryUrl? = null
    private var target : Directory? = null
    private var overwrite : Boolean? = null
    override fun build(): GitCloneAction = GitCloneAction(
        name = ActionName(name),
        repositoryUrl = requireNotNull(repositoryUrl) {"Missing repositoryUrl"},
        target = requireNotNull(target) {"Missing target"},
        overwrite = overwrite ?: true
    )

    fun repositoryUrl(path : String) {
        repositoryUrl = GitRepositoryUrl(path)
    }

    fun target(path : String) {
        target = Directory(path)
    }


}