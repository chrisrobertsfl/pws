package com.kohls.pws

class OmoSpec : StringSpec({
    "run" {
        val urlString = "https://jiradc.kohls.com:8443/browse/OMO-1906"
        val url = URL(urlString)

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"  // Optional, GET is the default method

            println("Response Code: $responseCode")
            println("Response Message: $responseMessage")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream.bufferedReader().use {
                    it.lines().forEach { line ->
                        println(line)
                    }
                }
            } else {
                println("Error: Failed to retrieve content from $urlString")
            }
        }
    }

})