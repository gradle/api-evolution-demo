package org.gradle.demo.api.evolution;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.objectweb.asm.Opcodes.ASM9;

class ApiUpgraderClassVisitor extends ClassVisitor {

    private final List<Replacement> replacements;

    public ApiUpgraderClassVisitor(List<Replacement> methodReplacements, ClassVisitor classVisitor) {
        super(ASM9, classVisitor);
        this.replacements = methodReplacements;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (cv == null) {
            return null;
        }

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodReplaceMethodVisitor(mv);
    }

    private final class MethodReplaceMethodVisitor extends MethodVisitor {

        public MethodReplaceMethodVisitor(MethodVisitor mv) {
            super(ASM9, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            for (int index = 0, len = replacements.size(); index < len; index++) {
                Replacement replacement = replacements.get(index);
                if (replacement.replaceIfMatches(opcode, owner, name, desc, itf, index, this)) {
                    return;
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
