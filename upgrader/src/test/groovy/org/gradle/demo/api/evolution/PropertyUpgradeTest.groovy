package org.gradle.demo.api.evolution

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import spock.lang.Unroll

import java.lang.reflect.Type

@Unroll
class PropertyUpgradeTest extends AbstractApiUpgradeSpec {

    def "raw #originalType.simpleName property in #description client can be upgraded to lazy property"() {
        def serverClass = compileNew """
            @CompileStatic
            class Server {
                final Property<${upgradedType.name}> testProperty = new Property<>(${originalValue})
            }
        """

        manager.matchProperty(serverClass, originalType, "testProperty")
            .replaceWith(
                { server -> server.getTestProperty().get() },
                { server, value -> server.getTestProperty().set(value) }
            )
        manager.init()

        compileOld("""
            @CompileStatic
            class Server {
                private ${originalType.name} value = ${originalValue}
                ${originalType.name} ${prefixFor(originalType)}TestProperty() {
                    value
                }
                void setTestProperty(${originalType.name} value) {
                    this.value = value
                }
            }
        """)

        def oldClient = compileAndUpgradeOld """
            ${dynamic ? "" : "@CompileStatic"}
            class Client {
                public void test() {
                    def server = new Server()
                    assert server.${prefixFor(originalType)}TestProperty() == ${originalValue}
                    server.setTestProperty(${changedValue})
                    assert server.${prefixFor(originalType)}TestProperty() == ${changedValue}

                    server.testProperty = ${originalValue}
                    assert server.testProperty == ${originalValue}
                }
            }
        """

        when:
        oldClient.newInstance().test()
        then:
        noExceptionThrown()

        where:
        [dynamic, [originalType, upgradedType, originalValue, changedValue]] << [
            [true, false],
            [
                [boolean, Boolean, false, true],
                [Boolean, Boolean, false, true],
                [byte, Byte, '(byte) 0', '(byte) 123'],
                [Byte, Byte, '(byte) 0', '(byte) 123'],
                [short, Short, '(short) 0', '(short) 123'],
                [Short, Short, '(short) 0', '(short) 123'],
                [int, Integer, 0, 123],
                [Integer, Integer, 0, 123],
                [float, Float, '0F', '123F'],
                [Float, Float, '0F', '123F'],
                [char, Character, "(char) 'a'", "(char) 'b'"],
                [Character, Character, "(Character) 'a'", "(Character) 'b'"],
                [String, String, '"original"', '"lajos"'],
                [Thing, Thing, "new Thing(1)", "new Thing(2)"],
                // TODO Long and double doesn't work yet -- https://github.com/gradle/api-evolution-demo/issues/11
//                [long, Long, '0L', '123L'],
//                [Long, Long, '0L', '123L'],
//                [double, Double, '0D', '123D'],
//                [Double, Double, '0D', '123D'],
            ]
        ].combinations()
        description = dynamic ? "dynamic" : "static"
    }

    private static String prefixFor(Type type) {
        type == boolean ? "is" : "get"
    }

    @CompileStatic
    @EqualsAndHashCode
    static class Thing {
        private int value

        Thing(int value) { this.value = value }
    }

    @CompileStatic
    static class Property<T> {
        private T value

        Property(T value) { set(value) }

        T get() { value }

        void set(T value) { this.value = value }
    }
}
