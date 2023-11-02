package com.kohls.pws.v2

sealed class Source

data class LocalSource(
    val path: String
) : Source()

data class GitSource(
    val url: String, val branch: String, val directory: String
) : Source()