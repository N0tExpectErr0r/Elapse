package com.n0texpecterr0r.elapseasm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by N0tExpectErr0r at 2019/08/08
 */
class ElapseClassVisitor extends ClassVisitor {
    public ElapseClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new ElapseMethodVisitor(methodVisitor, access, name, desc);
    }
}
