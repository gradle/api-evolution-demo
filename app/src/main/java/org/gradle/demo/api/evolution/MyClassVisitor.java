package org.gradle.demo.api.evolution;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class MyClassVisitor extends ClassVisitor {

    public MyClassVisitor(int i, ClassVisitor classVisitor) {
        super(i, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (cv == null) {
            return null;
        }

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ("<clinit>".equals(name)) {
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, "java/lang/System",
                "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("Hello World!");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream",
                "println", "(Ljava/lang/String;)V", false);
            mv.visitInsn(RETURN);
            mv.visitEnd();
        return mv;
        } else {
            return new MethodReplaceMethodVisitor(mv);
        }
    }

    private static final class MethodReplaceMethodVisitor extends MethodVisitor {

        public MethodReplaceMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM9, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            System.out.println(String.format("opcode: %d, owner: %s, name: %s, desc: %s, itf: %s",
                opcode, owner, name, desc, itf ? "true" : "false"));
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
