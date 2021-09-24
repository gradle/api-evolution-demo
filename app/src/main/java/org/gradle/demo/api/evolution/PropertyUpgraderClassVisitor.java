package org.gradle.demo.api.evolution;

import org.codehaus.groovy.runtime.callsite.CallSiteArray;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASM9;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

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
    private static final String CREATE_CALL_SITE_ARRAY_METHOD = "$createCallSiteArray";
    private static final String RETURN_CALL_SITE_ARRAY = getMethodDescriptor(getType(CallSiteArray.class));
    private static final String INSTRUMENTED_CALL_SITE_METHOD = "$instrumentedCallSiteArray";
    private static final Type INSTRUMENTED_TYPE = getType(Instrumented.class);
    private static final String RETURN_VOID_FROM_CALL_SITE_ARRAY = getMethodDescriptor(Type.VOID_TYPE, getType(CallSiteArray.class));
    private static final String[] NO_EXCEPTIONS = new String[0];

    private boolean hasGroovyCallSites;
    private String className;

    public PropertyUpgraderClassVisitor(int i, ClassVisitor classVisitor) {
        super(i, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (cv == null) {
            return null;
        }

        if (name.equals(CREATE_CALL_SITE_ARRAY_METHOD) && desc.equals(RETURN_CALL_SITE_ARRAY)) {
            hasGroovyCallSites = true;
        }

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodReplaceMethodVisitor(mv, className);
    }

    @Override
    public void visitEnd() {
        if (hasGroovyCallSites) {
            generateCallSiteFactoryMethod();
        }
        super.visitEnd();
    }
    private void generateCallSiteFactoryMethod() {
        new MethodVisitorScope(
            visitStaticPrivateMethod(INSTRUMENTED_CALL_SITE_METHOD, RETURN_CALL_SITE_ARRAY)
        ) {
            {
                visitCode();
                super.visitMethodInsn(INVOKESTATIC, className, CREATE_CALL_SITE_ARRAY_METHOD, RETURN_CALL_SITE_ARRAY, false);
                super.visitInsn(DUP);
                super.visitMethodInsn(INVOKESTATIC, INSTRUMENTED_TYPE.getInternalName(), "groovyCallSites", RETURN_VOID_FROM_CALL_SITE_ARRAY, false);
                super.visitInsn(ARETURN);
                visitMaxs(2, 0);
                visitEnd();
            }
        };
    }

    private MethodVisitor visitStaticPrivateMethod(String name, String descriptor) {
        return super.visitMethod(
            ACC_STATIC | ACC_SYNTHETIC | ACC_PRIVATE,
            name,
            descriptor,
            null,
            NO_EXCEPTIONS
        );
    }

    private static class MethodVisitorScope extends MethodVisitor {

        public MethodVisitorScope(MethodVisitor methodVisitor) {
            super(ASM9, methodVisitor);
        }
    }

    private static final class MethodReplaceMethodVisitor extends MethodVisitor {

        private final String className;

        public MethodReplaceMethodVisitor(MethodVisitor mv, String className) {
            super(ASM9, mv);
            this.className = className;
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

            if (owner.equals(className) && name.equals(CREATE_CALL_SITE_ARRAY_METHOD) && desc.equals(RETURN_CALL_SITE_ARRAY)) {
                _INVOKESTATIC(className, INSTRUMENTED_CALL_SITE_METHOD, RETURN_CALL_SITE_ARRAY);
                return;
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        private void _INVOKESTATIC(String owner, String name, String descriptor) {
            super.visitMethodInsn(INVOKESTATIC, owner, name, descriptor, false);
        }
    }
}
