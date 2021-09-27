package org.gradle.demo.api.evolution

class DynamicGroovyClient {
    static void main() {
        def server = new Server()
        doSet(server)
        println doGet(server)
    }

    private static void doSet(Object server) {
        server.setTestProperty("lajos")
        server.testProperty = "tibor"
    }

    private static Object doGet(Object server) {
        def a = server.getTestProperty()
        def b = server.testProperty
        assert a == b
        return b
    }
}
