package org.gradle.demo.api.evolution

class KotlinClient {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val server = Server()
            server.name = "lajos"
            println(server.name)
        }
    }
}
