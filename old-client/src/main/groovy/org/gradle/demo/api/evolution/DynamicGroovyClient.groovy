package org.gradle.demo.api.evolution

class DynamicGroovyClient {
    static void main() {
        def server = new Server()
        doSetString(server)
        println doGetString(server)
        doSetInteger(server)
        println doGetInteger(server)
        // TODO Fix this, apparently Groovy sends double values over as BigDecimal
//        doSetDouble(server)
//        println doGetDouble(server)
    }

    private static void doSetString(Object server) {
        server.setStringProperty("lajos")
        server.stringProperty = "tibor"
    }

    private static String doGetString(Object server) {
        def a = server.getStringProperty()
        def b = server.stringProperty
        assert a == b
        return b
    }

    private static void doSetInteger(Object server) {
        server.setIntegerProperty(321)
        server.integerProperty = 321
    }

    private static int doGetInteger(Object server) {
        def a = server.getIntegerProperty()
        def b = server.integerProperty
        assert a == b
        return b
    }

    private static void doSetDouble(Object server) {
        server.setDoubleProperty(321.0)
        server.doubleProperty = 321.0
    }

    private static int doGetDouble(Object server) {
        def a = server.getDoubleProperty()
        def b = server.doubleProperty
        assert a == b
        return b
    }
}
