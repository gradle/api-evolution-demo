package org.gradle.demo.api.evolution

class DynamicGroovyClient {
    static {
        println "Szevasz"
    }

    static void main() {
        def server = new Server()
        server.name = "lajos"
        println server.name
    }
}
