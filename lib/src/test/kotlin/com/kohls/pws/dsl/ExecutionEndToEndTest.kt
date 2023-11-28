package com.kohls.pws.dsl

import io.kotest.core.spec.style.StringSpec

// TODO: Need top level logging for better user experience given exceptions
// TODO: Need top level logging for better user experience given information


class ExecutionEndToEndTest : StringSpec({

    beforeTest {
        ActionRegistry.unregisterAll()
        ActionRegistry.register(GitCheckout::class) { GitCheckout(it) }
        ActionRegistry.register(GitClone::class) { GitClone(it) }
    }

    "Checkout and Run OLM Projects" {
        projectSet("OLM") {
            project("Config Server") {
                action<GitClone>("Config Server Clone") {
                    repositoryUrl("git@gitlab.com:kohls/scps/scf/olm/config-server.git")
                    target("/tmp/workspaces/config-server")

                }
                action<GitCheckout>("Config Server Checkout - influx_container") {
                    target("/tmp/workspaces/config-server")
                    branch("influx_container")
                }
            }
        }.execute()
    }
})
