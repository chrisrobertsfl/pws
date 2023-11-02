package com.kohls.pws

sealed class Source

object UnknownSource : Source()
data class LocalSource(
    val path: String
) : Source()

data class GitSource(
    val url: String, val branch: String, val directory: String
) : Source()