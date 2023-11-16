package com.kohls.pws

import org.slf4j.LoggerFactory

data class GitPrepare(override val name: String = generateName(), var overwrite: Boolean = true) : Action {
    var repositoryUrl: String? = null
    var targetDirectoryPath: String? = null
    var branchName: String = "main"
    private val logger by lazy { LoggerFactory.getLogger(Maven::class.java) }
    override fun perform(parameters: Parameters): Parameters {
        logger.trace("parameters are {}", parameters)
        logger.trace("this@GitPrepare.repositoryUrl is {}", repositoryUrl)
        val gitClone = GitClone("GitClone@GitPrepare:  $name").apply {
            logger.trace("(GitPrepare) repositoryUrl is {}", this@GitPrepare.repositoryUrl)
            targetDirectoryPath = this@GitPrepare.targetDirectoryPath
            repositoryUrl = this@GitPrepare.repositoryUrl
        }
        val gitCheckout = GitCheckout("GitCheckout@GitPrepare:  $name")
        return gitCheckout.perform(gitClone.perform(parameters))
    }
}