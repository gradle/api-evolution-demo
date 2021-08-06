package org.gradle.demo.api.evolution;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
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
        }
        return mv;
    }
}
