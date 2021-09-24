package org.gradle.demo.api.evolution;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ASM9;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.SWAP;

public class PropertyUpgraderClassVisitor extends ClassVisitor {

    private static final String SERVER_TYPE = "org/gradle/demo/api/evolution/Server";
    private static final String SET_TEST_PROPERTY_METHOD = "setTestProperty";
    private static final String SET_TEST_PROPERTY_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)).toString();
    private static final String GET_TEST_PROPERTY_METHOD = "getTestProperty";
    private static final String PROPERTY_TYPE = "org/gradle/demo/api/evolution/Property";
    private static final String OLD_GET_TEST_PROPERTY_DESC = Type.getMethodDescriptor(Type.getType(String.class)).toString();
    private static final String NEW_GET_TEST_PROPERTY_DESC = Type.getMethodDescriptor(Type.getType("L" + PROPERTY_TYPE + ";")).toString();
    private static final String SET_METHOD = "set";
    private static final String SET_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class));
    private static final String GET_METHOD = "get";
    private static final String GET_DESC = Type.getMethodDescriptor(Type.getType(Object.class));

    public PropertyUpgraderClassVisitor(int i, ClassVisitor classVisitor) {
        super(i, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (cv == null) {
            return null;
        }

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodReplaceMethodVisitor(mv);
    }

    private static final class MethodReplaceMethodVisitor extends MethodVisitor {

        public MethodReplaceMethodVisitor(MethodVisitor mv) {
            super(ASM9, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == INVOKEVIRTUAL) {
                if (owner.equals(SERVER_TYPE)) {
                    if (name.equals(SET_TEST_PROPERTY_METHOD) && desc.equals(SET_TEST_PROPERTY_DESC)) {
                        super.visitInsn(SWAP);
                        super.visitMethodInsn(INVOKEVIRTUAL, owner, GET_TEST_PROPERTY_METHOD, NEW_GET_TEST_PROPERTY_DESC, false);
                        super.visitInsn(SWAP);
                        super.visitMethodInsn(INVOKEVIRTUAL, PROPERTY_TYPE, SET_METHOD, SET_DESC, false);
                        return;
                    } else if (name.equals(GET_TEST_PROPERTY_METHOD) || desc.equals(OLD_GET_TEST_PROPERTY_DESC)) {
                        super.visitMethodInsn(INVOKEVIRTUAL, owner, GET_TEST_PROPERTY_METHOD, NEW_GET_TEST_PROPERTY_DESC, false);
                        super.visitMethodInsn(INVOKEVIRTUAL, PROPERTY_TYPE, GET_METHOD, GET_DESC, false);
                        super.visitTypeInsn(CHECKCAST, Type.getType(String.class).getInternalName());
                        return;
                    }
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
