package com.altamiracorp.lumify.javaCodeIngest;

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
}
