package org.gradle.demo.api.evolution

class DynamicGroovyClient {
    static void main() {
        def server = new Server()
        doSetString(server)
        doSetInteger(server)
        println doGetString(server)
        println doGetInteger(server)
    }

    private static void doSetString(Object server) {
        server.getStringProperty().set("lajos")
    }
    private static Object doGetString(Object server) {
        return server.getStringProperty().get()
    }

    private static void doSetInteger(Object server) {
        server.getIntegerProperty().set(321)
    }
    private static Object doGetInteger(Object server) {
        return server.getIntegerProperty().get()
    }
}
