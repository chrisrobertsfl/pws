package com.kohls.pws

import com.kohls.base.Directory
import com.kohls.pws.Action.Companion.NO_FIELDS
import java.util.*

data class ValidDirectoryExists(override val name : String  = UUID.randomUUID().toString(), val directory : Directory) : Action {
    override fun perform() : Map<String, Any> {
         if (!directory.exists()) {
             throw Exception("Invalid directory ${directory.path}")
         }
        return NO_FIELDS
    }
}