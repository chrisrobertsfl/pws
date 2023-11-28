package com.kohls.pws.model

data class GitRepositoryUrl(val path: String) {
    init {
        require(path.isNotBlank()) { "Git repository url path cannot be blank ->${path}<-" }
    }
}