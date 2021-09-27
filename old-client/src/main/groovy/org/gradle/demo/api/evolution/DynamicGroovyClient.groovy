package org.gradle.demo.api.evolution

class DynamicGroovyClient {
    static void main() {
        def server = new Server()
        doSet(server)
        println doGet(server)
    }

    private static void doSet(Object server) {
        server.setStringProperty("lajos")
        server.stringProperty = "tibor"
    }

    private static Object doGet(Object server) {
        def a = server.getStringProperty()
        def b = server.stringProperty
        assert a == b
        return b
    }
}
