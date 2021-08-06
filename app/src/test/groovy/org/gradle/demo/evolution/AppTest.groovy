package org.gradle.demo.evolution

import org.gradle.demo.api.evolution.DynamicGroovyClient
import org.gradle.demo.api.evolution.JavaClient
import org.gradle.demo.api.evolution.StaticGroovyClient
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import spock.lang.Specification

import java.lang.reflect.Method


class AppTest extends Specification {

    def setupSpec() {
        transformServerClass()
    }

    def "old Java client works with old api"() {
        when:
        JavaClient.main()

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

    private def transformServerClass() {
        // creates the ASM ClassReader which will read the class file
        ClassReader classReader = new ClassReader(getClass().classLoader.getResource("org/gradle/demo/api/evolution/DynamicGroovyClient.class").bytes);
        // creates the ASM ClassWriter which will create the transformed class
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
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
        Object server = clientClass.newInstance();
        Method getNameMethod = clientClass.getMethod("main", String[].class);
        // class the getNameMethod method
     //   println "calling main() on DynamicGroovyClient class yields: " + getNameMethod.invoke(null, new String[] {});
    }
}

class MyClassVisitor extends ClassVisitor {

    public MyClassVisitor(int i, ClassVisitor classVisitor) {
        super(i, classVisitor);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (cv == null) {
            return null;
        }

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        // <clinit> defines the static block in which the assignment of static variables happens.
        // E.g. private static final Logger logger = Logger.getLogger(HelloWorld.class.getName());
        // The assignment of the logger variable happens in <clinit>.
        if ("<init>".equals(name)) {
            return new StaticBlockMethodVisitor(mv);
        } else {
            // all other methods (static and none static)
            return  mv;
        }
    }

    class StaticBlockMethodVisitor extends MethodVisitor {
        StaticBlockMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM9, mv);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            // do the default behaviour: add the putstatic opcode to the byte code
            super.visitFieldInsn(opcode, owner, name, desc);
        }
    }
}
