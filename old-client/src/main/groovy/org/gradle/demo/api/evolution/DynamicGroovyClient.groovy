package org.gradle.demo.api.evolution

class DynamicGroovyClient {

    private static Object createServer() {
        return new Server()
    }

    static void main() {
        def server = createServer()
        server.setTestProperty("lajos")
        println server.getTestProperty()
    }
}
