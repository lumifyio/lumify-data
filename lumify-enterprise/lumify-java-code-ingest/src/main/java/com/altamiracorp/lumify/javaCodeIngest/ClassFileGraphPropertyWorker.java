package com.altamiracorp.lumify.javaCodeIngest;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.lumify.core.model.ontology.OntologyLumifyProperties;
import com.altamiracorp.lumify.core.model.properties.LumifyProperties;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.VertexBuilder;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.io.InputStream;

public class ClassFileGraphPropertyWorker extends GraphPropertyWorker {
    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        RawLumifyProperties.MIME_TYPE.setProperty(data.getVertex(), "application/x-java-class", data.getVertex().getVisibility());

        String fileName = RawLumifyProperties.FILE_NAME.getPropertyValue(data.getVertex());

        JavaClass javaClass = new ClassParser(in, fileName).parse();
        ConstantPoolGen constants = new ConstantPoolGen(javaClass.getConstantPool());

        Vertex classVertex = createClassVertex(javaClass, data);
        for (Method method : javaClass.getMethods()) {
            createMethodVertex(method, classVertex, javaClass, constants, data);
        }
        for (Field field : javaClass.getFields()) {
            createFieldVertex(field, classVertex, javaClass, data);
        }
    }

    private Vertex createClassVertex(JavaClass javaClass, GraphPropertyWorkData data) {
        String classId = JavaCodeIngestIdGenerator.createClassId(javaClass);
        VertexBuilder vertexBuilder = getGraph().prepareVertex(classId, data.getVertex().getVisibility(), getAuthorizations());
        LumifyProperties.TITLE.setProperty(vertexBuilder, classNameToTitle(javaClass.getClassName()), data.getProperty().getVisibility());
        Ontology.CLASS_NAME.setProperty(vertexBuilder, javaClass.getClassName(), data.getProperty().getVisibility());
        if (javaClass.isInterface()) {
            OntologyLumifyProperties.CONCEPT_TYPE.setProperty(vertexBuilder, Ontology.CONCEPT_TYPE_INTERFACE, data.getProperty().getVisibility());
        } else {
            OntologyLumifyProperties.CONCEPT_TYPE.setProperty(vertexBuilder, Ontology.CONCEPT_TYPE_CLASS, data.getProperty().getVisibility());
        }
        Vertex v = vertexBuilder.save();

        String containsClassEdgeId = JavaCodeIngestIdGenerator.createFileContainsClassEdgeId(data.getVertex(), v);
        getGraph().addEdge(containsClassEdgeId, data.getVertex(), v, Ontology.EDGE_LABEL_CLASS_FILE_CONTAINS_CLASS, data.getProperty().getVisibility(), getAuthorizations());

        return v;
    }

    private String classNameToTitle(String className) {
        int i = className.lastIndexOf('.');
        if (i < 0) {
            return className;
        }
        return className.substring(i + 1);
    }

    private void createMethodVertex(Method method, Vertex classVertex, JavaClass javaClass, ConstantPoolGen constants, GraphPropertyWorkData data) {
        String methodId = JavaCodeIngestIdGenerator.createMethodId(javaClass, method);
        VertexBuilder vertexBuilder = getGraph().prepareVertex(methodId, data.getVertex().getVisibility(), getAuthorizations());
        LumifyProperties.TITLE.setProperty(vertexBuilder, method.getName() + method.getSignature(), data.getProperty().getVisibility());
        OntologyLumifyProperties.CONCEPT_TYPE.setProperty(vertexBuilder, Ontology.CONCEPT_TYPE_METHOD, data.getProperty().getVisibility());
        Vertex methodVertex = vertexBuilder.save();

        String classContainsMethodEdgeId = JavaCodeIngestIdGenerator.createClassContainsMethodEdgeId(classVertex, methodVertex);
        getGraph().addEdge(classContainsMethodEdgeId, classVertex, methodVertex, Ontology.EDGE_LABEL_CLASS_CONTAINS, data.getProperty().getVisibility(), getAuthorizations());

        MethodGen mg = new MethodGen(method, javaClass.getClassName(), constants);
        if (mg.isAbstract() || mg.isNative()) {
            return;
        }
        ConstantPoolGen constantPool = mg.getConstantPool();
        for (InstructionHandle ih = mg.getInstructionList().getStart(); ih != null; ih = ih.getNext()) {
            Instruction i = ih.getInstruction();
            if (i instanceof InvokeInstruction) {
                InvokeInstruction ii = (InvokeInstruction) i;
                String methodClassName = ii.getClassName(constantPool);
                String methodName = ii.getMethodName(constantPool);
                String methodSignature = ii.getSignature(constantPool);
                String invokedMethodId = JavaCodeIngestIdGenerator.createMethodId(methodClassName, methodName, methodSignature);
                VertexBuilder invokedMethodVertexBuilder = getGraph().prepareVertex(invokedMethodId, data.getVertex().getVisibility(), getAuthorizations());
                LumifyProperties.TITLE.setProperty(invokedMethodVertexBuilder, method.getSignature(), data.getProperty().getVisibility());
                OntologyLumifyProperties.CONCEPT_TYPE.setProperty(invokedMethodVertexBuilder, Ontology.CONCEPT_TYPE_METHOD, data.getProperty().getVisibility());
                Vertex invokedMethodVertex = invokedMethodVertexBuilder.save();

                String methodInvokesMethodEdgeId = JavaCodeIngestIdGenerator.createMethodInvokesMethodEdgeId(methodVertex, invokedMethodVertex);
                getGraph().addEdge(methodInvokesMethodEdgeId, methodVertex, invokedMethodVertex, Ontology.EDGE_LABEL_INVOKED, data.getProperty().getVisibility(), getAuthorizations());
            }
        }
    }

    private void createFieldVertex(Field field, Vertex classVertex, JavaClass javaClass, GraphPropertyWorkData data) {
        String fieldId = JavaCodeIngestIdGenerator.createFieldId(javaClass, field);
        VertexBuilder vertexBuilder = getGraph().prepareVertex(fieldId, data.getVertex().getVisibility(), getAuthorizations());
        LumifyProperties.TITLE.setProperty(vertexBuilder, field.getName(), data.getProperty().getVisibility());
        OntologyLumifyProperties.CONCEPT_TYPE.setProperty(vertexBuilder, Ontology.CONCEPT_TYPE_FIELD, data.getProperty().getVisibility());
        Vertex v = vertexBuilder.save();

        String classContainsFieldEdgeId = JavaCodeIngestIdGenerator.createClassContainsFieldEdgeId(classVertex, v);
        getGraph().addEdge(classContainsFieldEdgeId, classVertex, v, Ontology.EDGE_LABEL_CLASS_CONTAINS, data.getProperty().getVisibility(), getAuthorizations());
    }

    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        if (!property.getName().equals(RawLumifyProperties.RAW.getKey())) {
            return false;
        }

        String fileName = RawLumifyProperties.FILE_NAME.getPropertyValue(vertex);
        if (fileName == null || !fileName.endsWith(".class")) {
            return false;
        }

        return true;
    }
}
