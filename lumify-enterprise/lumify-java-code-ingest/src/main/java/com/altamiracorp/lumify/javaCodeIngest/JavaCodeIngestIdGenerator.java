package com.altamiracorp.lumify.javaCodeIngest;

import com.altamiracorp.securegraph.Vertex;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

public class JavaCodeIngestIdGenerator {
    public static String createClassId(JavaClass javaClass) {
        return "CLASS_" + javaClass.getClassName();
    }

    public static String createMethodId(JavaClass javaClass, Method method) {
        return createMethodId(javaClass.getClassName(), method.getName(), method.getSignature());
    }

    public static String createMethodId(String methodClassName, String methodName, String methodSignature) {
        return "METHOD_" + methodClassName + "." + methodName + methodSignature;
    }

    public static String createFieldId(JavaClass javaClass, Field field) {
        return createMethodId(javaClass.getClassName(), field.getName());
    }

    public static String createMethodId(String className, String name) {
        return "FIELD_" + className + "." + name;
    }

    public static String createFileContainsClassEdgeId(Vertex fileVertex, Vertex classVertex) {
        return "FILE_CONTAINS_" + fileVertex.getId() + "-" + classVertex.getId();
    }

    public static String createClassContainsMethodEdgeId(Vertex classVertex, Vertex methodVertex) {
        return "CLASS_CONTAINS_METHOD_" + classVertex.getId() + "-" + methodVertex.getId();
    }

    public static String createClassContainsFieldEdgeId(Vertex classVertex, Vertex fieldVertex) {
        return "CLASS_CONTAINS_FIELD_" + classVertex.getId() + "-" + fieldVertex.getId();
    }

    public static String createMethodInvokesMethodEdgeId(Vertex methodVertex, Vertex invokedMethodVertex) {
        return "METHOD_INVOKES_" + methodVertex.getId() + "-" + invokedMethodVertex.getId();
    }
}
