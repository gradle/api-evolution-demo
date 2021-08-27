package org.gradle.demo.api.evolution

class DynamicGroovyClient {
    static void main() {
        println run(new Server())
    }

    private static Object run(server) {
        server.getTestProperty().set("lajos")
        return server.getTestProperty().get()
    }
}
