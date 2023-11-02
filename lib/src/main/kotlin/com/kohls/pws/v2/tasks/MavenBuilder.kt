package com.kohls.pws.v2.tasks

import com.kohls.pws.v2.IdGenerator
import com.kohls.pws.v2.Task
import com.kohls.pws.v2.TaskBuilder
import com.kohls.pws.v2.Validation
import com.kohls.pws.v2.validations.LogValidator
import java.io.File
import kotlin.time.Duration

class MavenBuilder(override val idGenerator: IdGenerator) : TaskBuilder {
    private val args = mutableListOf<String>()
    private val variables = mutableMapOf<String, String>()
    private var  background: Boolean = false
    private  var settingsXmlFilePath: File? = null
    private  var pomXmlFilePath: File? = null
    private val validations = mutableListOf<Validation>()
    override fun build(): Task {
        return Maven(idGenerator.generate(), args, variables, background, settingsXmlFilePath, pomXmlFilePath, validations)
    }

    fun MavenBuilder.maven(background: Boolean = false, block : MavenBuilder.() -> Unit) {
        this.background = background
        apply(block)
    }

    fun  args(vararg args : String) {
        this.args.addAll(args)
    }
    fun  proxy(proxy : String) {
        this.variables += "HTTPS_PROXY" to proxy
    }

    fun settings(settings : String) {
        settingsXmlFilePath = File(settings)
    }

    fun pom(pom : String) {
        pomXmlFilePath = File(pom)
    }

    fun validations(block : MavenBuilder.() -> Unit) {
        apply(block)
    }

    fun logContains(duration: Duration, text: String) { // TODO:  Right now it adds just one expand to many
        validations += LogValidator(duration, listOf(text))
    }
}