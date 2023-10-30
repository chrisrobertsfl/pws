package com.kohls.pws

sealed interface Source {
    object UNKNOWN : Source
    data class Local(val directory: Directory) : Source
    data class Git(val url: String, val branch: String, val directory: Directory) : Source
}