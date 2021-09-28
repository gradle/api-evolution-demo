package org.gradle.demo.api.evolution

import spock.lang.Specification

class AbstractApiUpgradeSpec extends Specification {
    protected ClassLoader originalClassLoader
    protected GroovyClassLoader oldClassLoader
    protected GroovyClassLoader newClassLoader

    protected ApiUpgradeManager manager = new ApiUpgradeManager()

    def setup() {
        originalClassLoader = Thread.currentThread().contextClassLoader
        oldClassLoader = new GroovyClassLoader(originalClassLoader)
        newClassLoader = new GroovyClassLoader(originalClassLoader)
        Thread.currentThread().contextClassLoader = this.newClassLoader
    }

    def cleanup() {
        Thread.currentThread().contextClassLoader = originalClassLoader
    }
}
