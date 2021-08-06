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
            println "Transforming classes"
            transformAndLoadDynamicGroovyClientClass()
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

    private def transformAndLoadDynamicGroovyClientClass() {
        // creates the ASM ClassReader which will read the class file
        ClassReader classReader = new ClassReader(getClass().classLoader.getResource("org/gradle/demo/api/evolution/DynamicGroovyClient.class").bytes);
        // creates the ASM ClassWriter which will create the transformed class
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        // creates the ClassVisitor to do the byte code transformations
        ClassVisitor classVisitor = new MyClassVisitor(Opcodes.ASM9, classWriter);
        // reads the class file and apply the transformations which will be written into the ClassWriter
        classReader.accept(classVisitor, 0);

        // gets the bytes from the transformed class
        byte[] bytes = classWriter.toByteArray();
        // writes the transformed class to the file system - to analyse it (e.g. javap -verbose)
        new FileOutputStream(new File("DynamicGroovyClient\$\$Transformed.class")).write(bytes);

        // inject the transformed class into the current class loader
        ClassLoader classLoader = getClass().getClassLoader();
        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        defineClass.setAccessible(true);
        Class<?> clientClass = (Class<?>) defineClass.invoke(classLoader, null, bytes, 0, bytes.length);

        // prober the server clas
        Object client = clientClass.newInstance();
        Method getNameMethod = clientClass.getMethod("main", String[].class);
        // class the getNameMethod method
     //   println "calling main() on DynamicGroovyClient class yields: " + getNameMethod.invoke(null, new String[] {});
    }
}

