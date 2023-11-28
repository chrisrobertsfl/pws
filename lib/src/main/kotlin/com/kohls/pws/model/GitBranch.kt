package com.kohls.pws.model

data class GitBranch(val name: String) {
    init {
        require(name.isNotBlank()) { "Git branch cannot be blank ->${name}<-" }
    }
}