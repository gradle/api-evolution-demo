package org.gradle.demo.api.evolution

import groovy.transform.CompileStatic

class PropertyUpgradeTest extends AbstractApiUpgradeSpec {

    def setup() {
        compileNew """
            @$CompileStatic.name
            class Property<T> {
                private T value
                Property(T value) { set(value) }
                T get() { value }
                void set(T value) { this.value = value }
            }
        """
    }

    def "string property can be made lazy"() {
        def serverClass = compileNew """
            @$CompileStatic.name
            class Server {
                private final Property<String> property = new Property<String>("init")
                Property<String> getString() { property }
            }
        """
        manager.matchProperty(serverClass, String, "string")
            .replaceWith(
                { server -> server.getString().get() },
                { server, value -> server.getString().set(value) }
            )
        manager.init()

        def oldServer = compileOld """
            @$CompileStatic.name
            class Server {
                private String value = "init"
                String getString() {
                    value
                }
                void setString(String value) {
                    this.value = value
                }
            }
        """
        def oldClient = compileAndUpgradeOld """
            @$CompileStatic.name
            class Client {
                public void test() {
                    println "Running test()"
                    def server = new Server()
                    assert server.getString() == "init"
                    server.setString("lajos")
                    assert server.getString() == "lajos"
                }
            }
        """
        when:
        oldClient.newInstance().test()
        then:
        noExceptionThrown()
    }
}
