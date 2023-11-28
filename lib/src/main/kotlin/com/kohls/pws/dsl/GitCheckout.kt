package com.kohls.pws.dsl

import com.kohls.base.Directory
import com.kohls.pws.model.ActionName
import com.kohls.pws.model.GitBranch
import com.kohls.pws.model.GitCheckoutAction

class GitCheckout(override val name : String) : ActionBuilder<GitCheckoutAction> {
    private var branch : GitBranch? = null
    private var target : Directory? = null
    override fun build(): GitCheckoutAction = GitCheckoutAction(name = ActionName(name), branch = branch ?: BRANCH, target = target)


    fun branch(branchName : String) {
        branch = GitBranch(branchName)
    }

    fun target(targetPath : String) {
        target = Directory(targetPath)
    }
    companion object {
        val BRANCH = GitBranch("main")
    }
}