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
        server.setStringProperty("lajos")
        server.stringProperty = "tibor"
    }

    private static void doSetInteger(Object server) {
        server.setIntegerProperty(321)
        server.integerProperty = 321
    }

    private static String doGetString(Object server) {
        def a = server.getStringProperty()
        def b = server.stringProperty
        assert a == b
        return b
    }

    private static int doGetInteger(Object server) {
        def a = server.getIntegerProperty()
        def b = server.integerProperty
        assert a == b
        return b
    }
}
