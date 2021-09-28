package org.gradle.demo.api.evolution

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.runtime.EncodingGroovyMethods
import org.codehaus.groovy.tools.GroovyClass
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


    protected Class<?> compileNew(String script) {
        def newClass = compile(newClassLoader, script)
        return newClassLoader.loadClass(newClass.name)
    }

    protected Class<?> compileAndUpgradeOld(String script) {
        def oldClass = compile(oldClassLoader, script)
        manager.implementReplacements(newClassLoader, oldClass.bytes)
        return newClassLoader.loadClass(oldClass.name)
    }

    protected Class<?> compileOld(String script) {
        def oldClass = compile(oldClassLoader, script)
        return oldClassLoader.loadClass(oldClass.name)
    }

    private static GroovyClass compile(GroovyClassLoader classLoader, String script) {
        def compileUnit = new CompilationUnit()
        def sourceUnit = compileUnit.addSource("Script_" + EncodingGroovyMethods.md5(script) + ".groovy", script)
        compileUnit.classLoader = classLoader
        compileUnit.compile(Phases.CLASS_GENERATION)

        GroovyClass target = null
        for (Object compileClass : compileUnit.getClasses()) {
            GroovyClass groovyClass = (GroovyClass) compileClass
            classLoader.defineClass(groovyClass.name, groovyClass.bytes)
            if (groovyClass.getName().equals(sourceUnit.ast.mainClassName)) {
                target = groovyClass
                break
            }
        }

        if (target == null) {
            throw new IllegalStateException("Could not find compiled class")
        }

        return target
    }
}
