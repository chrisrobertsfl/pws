package com.kohls.pws

import com.kohls.base.Directory
import java.util.*

data class ValidDirectoryExists(override val name : String  = UUID.randomUUID().toString(), val directory : Directory) : Action {
    override fun perform(parameters: Parameters) : Parameters {
         if (!directory.exists()) {
             throw Exception("Invalid directory ${directory.path}")
         }
        return Parameters.EMPTY
    }
}