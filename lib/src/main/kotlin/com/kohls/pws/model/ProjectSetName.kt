package com.kohls.pws.model

data class ProjectSetName(val contents: String) {
    init {
        require(contents.isNotBlank()) { "Project set name cannot be blank ->${contents}<-" }
    }
}