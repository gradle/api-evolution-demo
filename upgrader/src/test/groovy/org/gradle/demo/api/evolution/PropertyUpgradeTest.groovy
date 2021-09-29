package org.gradle.demo.api.evolution

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import spock.lang.Unroll

import java.lang.reflect.Type

@Unroll
class PropertyUpgradeTest extends AbstractApiUpgradeSpec {

    def "raw #originalType.simpleName property in #description client can be upgraded to lazy property"() {
        // This is the new server API to run against
        def newServerClass = compileNewApi """
            @CompileStatic
            class Server {
                final Property<${boxedType.name}> testProperty = new Property<>()
            }
        """

        // Register replacement of old getter/setter property
        manager.matchProperty(newServerClass, originalType, "testProperty")
            .replaceWith(
                { server -> server.getTestProperty().get() },
                { server, value -> server.getTestProperty().set(value) }
            )
        manager.init()

        // Add old server API so that we can compile the old client against it
        compileOldApi """
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

        // Compile old client against old server, then upgrade it and load it in the new class-loader
        def upgradedOldClientClass = compileAndUpgradeOldClient """
            ${dynamicClient ? "" : "@CompileStatic"}
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
        [dynamicClient, [originalType, boxedType, originalValue, changedValue]] << [
            [
                true,
                false
            ],
            [
                [boolean, Boolean, false, true],
                [Boolean, Boolean, false, true],
                [byte, Byte, (byte) 0, (byte) 123],
                [Byte, Byte, (byte) 0, (byte) 123],
                [short, Short, (short) 0, (short) 123],
                [Short, Short, (short) 0, (short) 123],
                [int, Integer, 0, 123],
                [Integer, Integer, 0, 123],
                [long, Long, 0L, 123L],
                [Long, Long, 0L, 123L],
                [float, Float, 0F, 123F],
                [Float, Float, 0F, 123F],
                [double, Double, 0D, 123D],
                [Double, Double, 0D, 123D],
                [char, Character, (char) 'a', (char) 'b'],
                [Character, Character, (Character) 'a', (Character) 'b'],
                [String, String, "original", "lajos"],
                [Thing, Thing, new Thing(1), new Thing(2)],
            ]
        ].combinations()
        description = dynamicClient ? "dynamic" : "static"
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

        T get() { value }

        void set(T value) { this.value = value }
    }
}
