package org.gradle.demo.api.evolution

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor
import groovy.transform.TypeCheckingMode
import spock.lang.Unroll

import java.lang.reflect.Type

@Unroll
class PropertyUpgradeTest extends AbstractApiUpgradeSpec {

    def "raw #originalType.simpleName property in #clientKind client can be upgraded to lazy property"() {
        // This is the new server API to run against
        def newServerClass = compileNewApi declareNewServer(boxedType)

        // Register replacement of old getter/setter property
        manager.matchProperty(newServerClass, originalType, "testProperty")
            .replaceWith(
                { server -> server.getTestProperty().get() },
                { server, value -> server.getTestProperty().set(value) }
            )
        manager.init()

        // Add old server API so that we can compile the old client against it
        compileOldApi declareOldServer(originalType)

        // Compile old client against old server, then upgrade it and load it in the new class-loader
        def upgradedOldClientClass = compileAndUpgradeOldClient """
            @CompileStatic(TypeCheckingMode.${clientTypeCheckingMode})
            @TupleConstructor
            class Client {
                Server server

                ${originalType.name} getUsingGetter() {
                    return server.${prefixFor(originalType)}TestProperty()
                }

                void setUsingSetter(${originalType.name} value) {
                    server.setTestProperty(value)
                }

                ${originalType.name} getUsingGroovyProperty() {
                    return server.testProperty
                }

                void setUsingGroovyProperty(${originalType.name} value) {
                    server.testProperty = value
                }
            }
        """

        def server = newServerClass.newInstance()
        def upgradedOldClient = upgradedOldClientClass.getConstructor(newServerClass).newInstance(server)

        when:
        upgradedOldClient.setUsingSetter(originalValue)
        then:
        upgradedOldClient.getUsingGetter() == originalValue
        upgradedOldClient.getUsingGroovyProperty() == originalValue

        when:
        upgradedOldClient.setUsingGroovyProperty(changedValue)
        then:
        upgradedOldClient.getUsingGetter() == changedValue
        upgradedOldClient.getUsingGroovyProperty() == changedValue

        where:
        [clientTypeCheckingMode, [originalType, boxedType, originalValue, changedValue]] << CLIENTS_UNDER_TEST
        clientKind = clientTypeCheckingMode == TypeCheckingMode.SKIP ? "dynamic" : "static"
    }

    private static declareOldServer(Type originalType) {
        """
            @CompileStatic
            class Server {
                private ${originalType.name} value
                ${originalType.name} ${prefixFor(originalType)}TestProperty() {
                    value
                }
                void setTestProperty(${originalType.name} value) {
                    this.value = value
                }
            }
        """
    }

    private static declareNewServer(Class<?> boxedType) {
        """
            @CompileStatic
            class Server {
                final Property<${boxedType.name}> testProperty = new Property<>()
            }
        """
    }

    private static String prefixFor(Type type) {
        type == boolean ? "is" : "get"
    }

    private static <B> List<?> upgrade(Type originalType, Class<B> boxedType, B originalValue, B changedValue) {
        [originalType, boxedType, originalValue, changedValue]
    }

    @CompileStatic
    @EqualsAndHashCode
    @TupleConstructor
    static class Thing {
        int value
    }

    @CompileStatic
    static class Property<T> {
        private T value

        T get() { value }

        void set(T value) { this.value = value }
    }

    static final UPGRADES_UNDER_TEST = [
        upgrade(boolean, Boolean, false, true),
        upgrade(Boolean, Boolean, false, true),
        upgrade(byte, Byte, (byte) 7, (byte) 123),
        upgrade(Byte, Byte, (byte) 7, (byte) 123),
        upgrade(short, Short, (short) 7, (short) 123),
        upgrade(Short, Short, (short) 7, (short) 123),
        upgrade(int, Integer, 7, 123),
        upgrade(Integer, Integer, 7, 123),
        upgrade(long, Long, 7L, 123L),
        upgrade(Long, Long, 7L, 123L),
        upgrade(float, Float, 7F, 123F),
        upgrade(Float, Float, 7F, 123F),
        upgrade(double, Double, 7D, 123D),
        upgrade(Double, Double, 7D, 123D),
        upgrade(char, Character, (char) 'a', (char) 'b'),
        upgrade(Character, Character, (Character) 'a', (Character) 'b'),
        upgrade(String, String, "original", "lajos"),
        upgrade(Thing, Thing, new Thing(7), new Thing(123)),
    ]

    static final CLIENTS_UNDER_TEST = [
        [
            // Simulate client written using statically compiled language
            TypeCheckingMode.PASS,
            // Dynamic Groovy client
            TypeCheckingMode.SKIP
        ],
        UPGRADES_UNDER_TEST
    ].combinations()

}
