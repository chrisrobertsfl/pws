package com.kohls.pws

import java.util.*

fun generateName() = UUID.randomUUID().toString()
fun emptyString() = ""


/**
 * TODO:  All actions should end with Action and should have a counterpart DSL Builder version so for example:
 * Given an action called Maven this is the convention:
 * 1.  DSL Builder will be called Maven
 * 2.  Action will be called MavenAction
 *
 */
