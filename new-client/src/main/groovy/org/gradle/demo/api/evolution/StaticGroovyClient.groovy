package org.gradle.demo.api.evolution

import groovy.transform.CompileStatic

@CompileStatic
class StaticGroovyClient {
    static void main() {
        println run(new Server())
    }

    public static String run(Server server) {
        server.testProperty.set("lajos")
        return server.testProperty.get()
    }
}
