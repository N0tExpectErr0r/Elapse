package com.n0texpecterr0r.elapseasm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Created by N0tExpectErr0r at 2019/08/08
 */
class ElapseMethodVisitor extends AdviceAdapter {
    private static final String ANNOTATION_TRACK_METHOD = "Lcom/n0texpecterr0r/elapse/TrackMethod;";
    private static final String METHOD_EVENT_MANAGER = "com/n0texpecterr0r/elapse/MethodEventManager";
    private final MethodVisitor methodVisitor;
    private final String methodName;

    private boolean needInject;
    private String tag;

    public ElapseMethodVisitor(MethodVisitor methodVisitor, int access, String name, String desc) {
        super(Opcodes.ASM6, methodVisitor, access, name, desc);
        this.methodVisitor = methodVisitor;
        this.methodName = name;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationVisitor annotationVisitor = super.visitAnnotation(desc, visible);
        if (desc.equals(ANNOTATION_TRACK_METHOD)) {
            needInject = true;
            return new AnnotationVisitor(Opcodes.ASM6, annotationVisitor) {
                @Override
                public void visit(String name, Object value) {
                    super.visit(name, value);
                    if (name.equals("tag") && value instanceof String) {
                        tag = (String) value;
                    }
                }
            };
        }
        return annotationVisitor;
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        handleMethodEnter();
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        handleMethodExit();
    }

    private void handleMethodEnter() {
        if (needInject && tag != null) {
            methodVisitor.visitMethodInsn(INVOKESTATIC, METHOD_EVENT_MANAGER,
                    "getInstance", "()L"+METHOD_EVENT_MANAGER+";", false);
            methodVisitor.visitLdcInsn(tag);
            methodVisitor.visitLdcInsn(methodName);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, METHOD_EVENT_MANAGER,
                    "notifyMethodEnter", "(Ljava/lang/String;Ljava/lang/String;)V", false);
        }
    }

    private void handleMethodExit() {
        if (needInject && tag != null) {
            methodVisitor.visitMethodInsn(INVOKESTATIC, METHOD_EVENT_MANAGER,
                    "getInstance", "()L"+METHOD_EVENT_MANAGER+";", false);
            methodVisitor.visitLdcInsn(tag);
            methodVisitor.visitLdcInsn(methodName);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, METHOD_EVENT_MANAGER,
                    "notifyMethodExit", "(Ljava/lang/String;Ljava/lang/String;)V", false);
        }
    }
}
