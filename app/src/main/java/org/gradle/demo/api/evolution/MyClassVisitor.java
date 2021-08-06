package org.gradle.demo.api.evolution;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MyClassVisitor extends ClassVisitor {

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
