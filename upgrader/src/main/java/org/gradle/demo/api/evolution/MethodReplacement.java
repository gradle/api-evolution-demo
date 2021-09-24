package org.gradle.demo.api.evolution;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

class MethodReplacement<T> implements Replacement {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodReplacement.class);

    private static final String INVOKE_REPLACEMENT_DESC;

    static {
        try {
            INVOKE_REPLACEMENT_DESC = Type.getMethodDescriptor(ApiUpgradeManager.class.getMethod("invokeReplacement", int.class, Object.class, Object[].class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final Type type;
    private final Type returnType;
    private final String methodName;
    private final Type[] argumentTypes;
    private final String methodDescriptor;
    private final ReplacementLogic<T> replacement;

    public MethodReplacement(Type type, Type returnType, String methodName, Type[] argumentTypes, ReplacementLogic<T> replacement) {
        this.type = type;
        this.returnType = returnType;
        this.methodName = methodName;
        this.argumentTypes = argumentTypes;
        this.methodDescriptor = Type.getMethodDescriptor(returnType, argumentTypes).toString();
        this.replacement = replacement;
    }

    public T invokeReplacement(Object receiver, Object[] arguments) {
        LOGGER.info("Calling replacement for {}.{}({})", type, methodName, methodDescriptor);
        return replacement.execute(receiver, arguments);
    }

    @Override
    public boolean replaceIfMatches(int opcode, String owner, String name, String desc, boolean itf, int index, MethodVisitor mv) {
        if (opcode == INVOKEVIRTUAL
            && owner.equals(type.getInternalName())
            && name.equals(methodName)
            && desc.equals(methodDescriptor)) {

            LOGGER.info("Matched {}.{}({}), replacing...", owner, name, desc);

            // Read the stack (this + arguments) into local variables
            mv.visitFrame(Opcodes.F_NEW, argumentTypes.length + 1, argumentTypes, 0, new Object[0]);
            for (int i = argumentTypes.length; i >= 0; i--) {
                mv.visitVarInsn(ASTORE, i);
            }
            // Put the index of the replacement on the stack
            mv.visitLdcInsn(index);
            // Put 'this' back on stack
            mv.visitVarInsn(ALOAD, 0);

            // Put the arguments back on stack as an Object[]
            mv.visitLdcInsn(argumentTypes.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            for (int i = 1; i <= argumentTypes.length; i++) {
                mv.visitInsn(DUP);
                mv.visitLdcInsn(i - 1);
                mv.visitVarInsn(ALOAD, i);
                mv.visitInsn(AASTORE);
            }

            // Call the replacement method
            mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(ApiUpgradeManager.class), "invokeReplacement", INVOKE_REPLACEMENT_DESC, false);

            // Re-cast the returned value
            Type returnType = Type.getReturnType(desc);
            mv.visitTypeInsn(CHECKCAST, returnType.getDescriptor());
            return true;
        } else {
            return false;
        }
    }
}
