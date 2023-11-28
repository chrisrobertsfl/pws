package com.kohls.pws.model

data class ProjectName(val contents: String) {
    init {
        require(contents.isNotBlank()) { "Project name cannot be blank ->${contents}<-" }
    }
}