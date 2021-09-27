package org.gradle.demo.api.evolution;

import org.codehaus.groovy.runtime.callsite.CallSite;
import org.objectweb.asm.MethodVisitor;

import java.util.Optional;

public interface Replacement {
    /**
     * Replace the given instruction if it matches the replacement.
     *
     * @return {@code true} if the instruction has been replaced.
     */
    boolean replaceByteCodeIfMatches(int opcode, String owner, String name, String desc, boolean itf, int index, MethodVisitor mv);

    /**
     * Decorate the given Groovy call site during runtime.
     */
    Optional<CallSite> decorateCallSite(CallSite callSite);

    /**
     * Decorates the Groovy meta-class if necessary during runtime.
     */
    default void decorateMetaClass() {}
}
