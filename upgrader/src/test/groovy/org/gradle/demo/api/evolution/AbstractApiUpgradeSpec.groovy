package org.gradle.demo.api.evolution

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.runtime.EncodingGroovyMethods
import org.codehaus.groovy.tools.GroovyClass
import org.intellij.lang.annotations.Language
import spock.lang.Specification

/**
 * Abstract base test class to introduce tools that allow testing old code
 * compiled against other old code upgraded to work against new code.
 */
class AbstractApiUpgradeSpec extends Specification {
    private static final String STATIC_SCRIPT_PREFIX = "import groovy.transform.*\n"

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

    /**
     * Compile a class against the new class-loader and then load it.
     */
    protected Class<?> compileNew(@Language(value = "groovy", prefix = STATIC_SCRIPT_PREFIX) String script) {
        def newClass = compile(newClassLoader, script)
        return newClassLoader.loadClass(newClass.name)
    }

    /**
     * Compile a class against the old class-loader, then re-define it
     * in the new class-loader, then upgrade it, and then load it.
     */
    protected Class<?> compileAndUpgradeOld(@Language(value = "groovy", prefix = STATIC_SCRIPT_PREFIX) String script) {
        def oldClass = compile(oldClassLoader, script)
        manager.implementReplacements(newClassLoader, oldClass.bytes)
        return newClassLoader.loadClass(oldClass.name)
    }

    /**
     * Compile a class against the old class-loader and then load it.
     */
    protected Class<?> compileOld(@Language(value = "groovy", prefix = STATIC_SCRIPT_PREFIX) String script) {
        def oldClass = compile(oldClassLoader, script)
        return oldClassLoader.loadClass(oldClass.name)
    }

    private GroovyClass compile(GroovyClassLoader classLoader, String script) {
        def fullScript = STATIC_SCRIPT_PREFIX + "import ${getClass().name}.*\n" + script
        println "Compiling:\n" + fullScript

        def compileUnit = new CompilationUnit()
        def sourceUnit = compileUnit.addSource("Script_" + EncodingGroovyMethods.md5(script) + ".groovy", fullScript)
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
