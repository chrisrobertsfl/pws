package com.kohls.pws.dsl

import io.kotest.core.spec.style.StringSpec

// TODO: Need top level logging for better user experience given exceptions
// TODO: Need top level logging for better user experience given information


class ExecutionEndToEndTest : StringSpec({

    beforeTest {
        ActionRegistry.unregisterAll()
        ActionRegistry.register(GitCheckout::class) { GitCheckout(it) }
    }

    "Checkout and Run OLM Projects" {
        projectSet("OLM") {
            project("Config Server") {
                action<GitCheckout>("Config Server Checkout - influx_container") {
                    target("/Users/TKMA5QX/projects/olm-meta-repo/config-server")
                    branch("influx_container")
                }
            }
        }.execute()
    }
})
