package org.gradle.demo.api.evolution;

import org.codehaus.groovy.runtime.callsite.AbstractCallSite;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2_X1;
import static org.objectweb.asm.Opcodes.DUP_X2;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.POP;

/**
 * Replaces a method call with alternative code.
 * <p>
 * Replaces both statically compiled calls and dynamic Groovy call sites.
 */
class MethodReplacement<T> implements Replacement {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodReplacement.class);

    private static final String INVOKE_REPLACEMENT_DESC;

    static {
        try {
            INVOKE_REPLACEMENT_DESC = Type.getMethodDescriptor(ApiUpgradeManager.class.getMethod("invokeReplacement", Object.class, Object[].class, int.class));
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
    public boolean replaceByteCodeIfMatches(int opcode, String owner, String name, String desc, boolean itf, int index, MethodVisitor mv) {
        if (opcode == INVOKEVIRTUAL
            && owner.equals(type.getInternalName())
            && name.equals(methodName)
            && desc.equals(methodDescriptor)) {

            LOGGER.info("Matched {}.{}({}), replacing...", owner, name, desc);

            // Create Object[] for arguments
            mv.visitLdcInsn(argumentTypes.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

            // Convert the arguments on stack to Object[]
            for (int argumentIndex = argumentTypes.length - 1; argumentIndex >= 0; argumentIndex--) {
                // this, ..., arg, [] -> this, ..., arg, [], argIndex
                mv.visitLdcInsn(argumentIndex);
                // this, ..., arg, [], argIndex -> this, ..., [], argIndex, arg, [], argIndex
                mv.visitInsn(DUP2_X1);
                // this, ..., [], argIndex, arg, [], argIndex -> this, ..., [], argIndex, arg, []
                mv.visitInsn(POP);
                // this, ..., [], argIndex, arg, [] -> this, ..., [], [], argIndex, arg, []
                mv.visitInsn(DUP_X2);
                // this, ..., [], [], argIndex, arg, [] -> this, ..., [], [], argIndex, arg
                mv.visitInsn(POP);
                // this, ..., [], [], argIndex, arg -> this, ..., []
                mv.visitInsn(AASTORE);
            }

            // Put the replacement index on stack
            mv.visitLdcInsn(index);

            // Call the replacement method
            mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(ApiUpgradeManager.class), "invokeReplacement", INVOKE_REPLACEMENT_DESC, false);

            // Re-cast the returned value
            Type returnType = Type.getReturnType(desc);
            mv.visitTypeInsn(CHECKCAST, returnType.getInternalName());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<CallSite> decorateCallSite(CallSite callSite) {
        if (callSite.getName().equals(methodName)) {
            return Optional.of(new AbstractCallSite(callSite) {
                @Override
                public Object call(Object receiver, Object[] args) throws Throwable {
                    return replacement.execute(receiver, args);
                }
            });
        } else {
            return Optional.empty();
        }
    }
}
