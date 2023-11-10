package com.kohls.pws

import com.kohls.base.Directory

sealed interface Source {
    val path: Directory
}

object UnknownSource : Source {
    override val path: Directory get() = throw UnsupportedOperationException("Unknown source has no path")
}

data class LocalSource(override val path: Directory) : Source
data class GitSource(override val path: Directory, val url: String, val branch: String) : Source