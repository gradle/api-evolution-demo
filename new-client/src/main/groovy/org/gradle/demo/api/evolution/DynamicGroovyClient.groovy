package org.gradle.demo.api.evolution

class DynamicGroovyClient {
    static void main() {
        def server = new Server()
        doSet(server)
        println doGet(server)
    }

    private static void doSet(Object server) {
        server.getTestProperty().set("lajos")
    }
    private static Object doGet(Object server) {
        return server.getTestProperty().get()
    }
}
