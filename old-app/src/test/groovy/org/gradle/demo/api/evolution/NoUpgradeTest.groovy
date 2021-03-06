package org.gradle.demo.api.evolution

import spock.lang.Specification

class NoUpgradeTest extends Specification {

    def "Java client works"() {
        when:
        JavaClient.main()

        then:
        noExceptionThrown()
    }

    def "Kotlin client works"() {
        when:
        KotlinClient.main()

        then:
        noExceptionThrown()
    }

    def "static Groovy client works"() {
        when:
        StaticGroovyClient.main()

        then:
        noExceptionThrown()
    }

    def "dynamic Groovy client works"() {
        when:
        DynamicGroovyClient.main()

        then:
        noExceptionThrown()
    }
}
