package com.kohls.pws

import com.kohls.base.Directory
import java.util.*

data class ValidDirectoryExists(override val name : String  = UUID.randomUUID().toString(), val directory : Directory) : Action {
    override fun execute()  {
         if (!directory.exists()) {
             throw RuntimeException("Invalid directory ${directory.path}")
         }
    }
}