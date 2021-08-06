package org.gradle.demo.api.evolution

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import spock.lang.Specification

import java.lang.reflect.Method

class AppTest extends Specification {

    def setupSpec() {
        if (Boolean.getBoolean("org.gradle.api.transform")) {
            transformAndLoadClientClasses()
        }
    }

    def "old Java client works with old api"() {
        when:
        JavaClient.main()

        then:
        noExceptionThrown()
    }

    def "old Kotlin client works with old api"() {
        when:
        KotlinClient.main()

        then:
        noExceptionThrown()
    }

    def "old static Groovy client works with old api"() {
        when:
        StaticGroovyClient.main()

        then:
        noExceptionThrown()
    }

    def "old dynamic Groovy client works with old api"() {
        when:
        DynamicGroovyClient.main()

        then:
        noExceptionThrown()
    }

    private static def transformAndLoadClientClasses() {
        ["JavaClient", "KotlinClient", "DynamicGroovyClient", "StaticGroovyClient"]
            .forEach(AppTest::transformAndLoadClientClass)
    }

    private static def transformAndLoadClientClass(String className) {
        println "Transforming ${className}"
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // creates the ASM ClassReader which will read the class file
        ClassReader classReader = new ClassReader(classLoader.getResource("org/gradle/demo/api/evolution/${className}.class").bytes);
        // creates the ASM ClassWriter which will create the transformed class
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        // creates the ClassVisitor to do the byte code transformations
        ClassVisitor classVisitor = new MyClassVisitor(Opcodes.ASM9, classWriter);
        // reads the class file and apply the transformations which will be written into the ClassWriter
        classReader.accept(classVisitor, 0);

        // gets the bytes from the transformed class
        byte[] bytes = classWriter.toByteArray();
        // writes the transformed class to the file system - to analyse it (e.g. javap -verbose)
        //new FileOutputStream(new File("DynamicGroovyClient\$\$Transformed.class")).write(bytes);

        // inject the transformed class into the current class loader
        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        defineClass.setAccessible(true);
        defineClass.invoke(classLoader, null, bytes, 0, bytes.length);
    }
}
