package com.kohls.pws

import com.kohls.base.Directory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class GitClone(
    override val name: String = generateName(),
) : Action, ActionConfiguration<GitClone> by GitCloneConfig(name) {

    private val logger: Logger = LoggerFactory.getLogger(GitClone::class.java)

    var repositoryUrl: String
        get() = (this as GitCloneConfig).repositoryUrl
        set(value) { (this as GitCloneConfig).repositoryUrl = value }

    var targetDirectory: Directory
        get() = Directory((this as GitCloneConfig).targetDirectory)
        set(value) { (this as GitCloneConfig).targetDirectory = value.path }

    var overwrite: Boolean = true

    override fun perform(parameters: Parameters): Parameters {
        if (targetDirectory.exists()) {
            when (overwrite) {
                true -> targetDirectory.delete()
                false -> throw IllegalStateException("targetDirectory '${targetDirectory.path}' already exists and cannot be overwritten when overwrite is enabled")
            }
        }
        val executableScript = BashScript(commandName = "git-clone", body = Body.fromResource("/bash-scripts/git-clone.sh")).createExecutableScript()
        executableScript.execute(listOf(repositoryUrl, targetDirectory.path))
        return Parameters.create("targetDirectory" to targetDirectory)
    }
}

data class GitCloneConfig(override val name: String) : ActionConfiguration<GitClone> {
    lateinit var repositoryUrl: String
    lateinit var targetDirectory: String

    override fun configure(): GitClone {
        return GitClone(name).apply {
            this.repositoryUrl = this@GitCloneConfig.repositoryUrl
            this.targetDirectory = Directory(this@GitCloneConfig.targetDirectory)
        }
    }

    fun repositoryUrl(repositoryUrl: String) {
        this.repositoryUrl = repositoryUrl
    }

    fun targetDirectory(targetDirectory: String) {
        this.targetDirectory = targetDirectory
    }
}
