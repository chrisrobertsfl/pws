package com.kohls.pws.model

data class ActionName(val contents: String) {
    init {
        require(contents.isNotBlank()) { "Action name cannot be blank ->${contents}<-" }
    }
}