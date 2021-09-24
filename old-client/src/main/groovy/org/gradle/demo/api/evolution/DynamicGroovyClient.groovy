package org.gradle.demo.api.evolution

class DynamicGroovyClient {
    static void main() {
        def server = new Server()
        doSet(server)
        println doGet(server)

        doSetAsProperty(server)
        println doGetAsProperty(server)
    }

    private static void doSet(Object server) {
        server.setTestProperty("lajos")
    }
    private static Object doGet(Object server) {
        return server.getTestProperty()
    }

    private static Object doGetAsProperty(Object server) {
        return server.testProperty
    }

    private static Object doSetAsProperty(Object server) {
        return server.testProperty = "Лайош"
    }
}
