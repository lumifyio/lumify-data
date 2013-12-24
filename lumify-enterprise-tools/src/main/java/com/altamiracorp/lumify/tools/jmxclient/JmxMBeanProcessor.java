package com.altamiracorp.lumify.tools.jmxclient;

import javax.management.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class JmxMBeanProcessor {

    public abstract ProcessResult process(MBeanServerConnection mbeanServerConnection, String source, ObjectName mbeanName) throws Exception;

    protected static Map<String, Object> getAttributes(MBeanServerConnection mbeanServerConnection, ObjectName mbeanName) throws IntrospectionException, ReflectionException, InstanceNotFoundException, IOException {
        Map<String, Object> results = new HashMap<String, Object>();
        MBeanInfo info = mbeanServerConnection.getMBeanInfo(mbeanName);
        ArrayList<String> attributeNames = new ArrayList<String>();
        for (MBeanAttributeInfo attr : info.getAttributes()) {
            attributeNames.add(attr.getName());
        }
        AttributeList attributes = mbeanServerConnection.getAttributes(mbeanName, attributeNames.toArray(new String[0]));
        for (Object attributeObject : attributes) {
            Attribute attribute = (Attribute) attributeObject;
            results.put(attribute.getName(), attribute.getValue());
        }
        return results;
    }

    public static class ProcessResult {
        private final String group;
        private final String name;
        private final String source;
        private final List<ProcessResultColumn> columns;
        private final boolean hasTotalLine;

        public ProcessResult(String group, String name, String source, List<ProcessResultColumn> columns, boolean hasTotalLine) {
            this.group = group;
            this.name = name;
            this.source = source;
            this.columns = columns;
            this.hasTotalLine = hasTotalLine;
        }

        public String getGroup() {
            return group;
        }

        public String getName() {
            return name;
        }

        public String getSource() {
            return source;
        }

        public List<ProcessResultColumn> getColumns() {
            return columns;
        }

        public boolean isHasTotalLine() {
            return hasTotalLine;
        }
    }

    public static class ProcessResultColumn {
        public static final int ALIGN_LEFT = -1;
        public static final int ALIGN_CENTER = 0;
        public static final int ALIGN_RIGHT = 1;

        private final String name;
        private final Object value;
        private final ProcessResultColumnTotal total;
        private final int alignment;

        public ProcessResultColumn(String name, Object value, ProcessResultColumnTotal total) {
            this(name, value, total, ALIGN_RIGHT);
        }

        public ProcessResultColumn(String name, Object value, ProcessResultColumnTotal total, int alignment) {
            this.name = name;
            this.value = value;
            this.total = total;
            this.alignment = alignment;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public ProcessResultColumnTotal getTotal() {
            return total;
        }

        public int getAlignment() {
            return alignment;
        }

        @Override
        public String toString() {
            Object val = getValue();
            if (val == null) {
                return null;
            }
            if (val instanceof Double) {
                return String.format("%.2f", (Double) val);
            }
            return val.toString();
        }
    }

    public static abstract class ProcessResultColumnTotal {
        public static final ProcessResultColumnTotalNone None = new ProcessResultColumnTotalNone();
        public static final ProcessResultColumnTotalSum Sum = new ProcessResultColumnTotalSum();
        public static final ProcessResultColumnTotalMax Max = new ProcessResultColumnTotalMax();
        public static final ProcessResultColumnTotalMin Min = new ProcessResultColumnTotalMin();
        public static final ProcessResultColumnTotalAverage Average = new ProcessResultColumnTotalAverage();

        public abstract String getTotal(List<Object> columnData);
    }

    public static class ProcessResultColumnTotalNone extends ProcessResultColumnTotal {
        @Override
        public String getTotal(List<Object> columnData) {
            return "";
        }
    }

    public static class ProcessResultColumnTotalSum extends ProcessResultColumnTotal {

        @Override
        public String getTotal(List<Object> columnData) {
            if (columnData.get(0) == null) {
                throw new RuntimeException("Could not determine type because of null");
            }
            if (columnData.get(0) instanceof Long) {
                long l = 0;
                for (Object o : columnData) {
                    l += (Long) o;
                }
                return String.format("%d", l);
            } else if (columnData.get(0) instanceof Double) {
                double d = 0;
                for (Object o : columnData) {
                    d += (Double) o;
                }
                return String.format("%.2f", d);
            } else {
                throw new RuntimeException("Unhandled values of type " + columnData.get(0).getClass().getName());
            }
        }
    }

    public static class ProcessResultColumnTotalMax extends ProcessResultColumnTotal {

        @Override
        public String getTotal(List<Object> columnData) {
            if (columnData.get(0) == null) {
                throw new RuntimeException("Could not determine type because of null");
            }
            if (columnData.get(0) instanceof Long) {
                long l = 0;
                for (Object o : columnData) {
                    l = Math.max(l, (Long) o);
                }
                return String.format("%d", l);
            } else if (columnData.get(0) instanceof Double) {
                double d = 0;
                for (Object o : columnData) {
                    d = Math.max(d, (Double) o);
                }
                return String.format("%.2f", d);
            } else {
                throw new RuntimeException("Unhandled values of type " + columnData.get(0).getClass().getName());
            }
        }
    }

    public static class ProcessResultColumnTotalMin extends ProcessResultColumnTotal {

        @Override
        public String getTotal(List<Object> columnData) {
            if (columnData.get(0) == null) {
                throw new RuntimeException("Could not determine type because of null");
            }
            if (columnData.get(0) instanceof Long) {
                long l = 0;
                for (Object o : columnData) {
                    l = Math.min(l, (Long) o);
                }
                return String.format("%d", l);
            } else if (columnData.get(0) instanceof Double) {
                double d = 0;
                for (Object o : columnData) {
                    d = Math.min(d, (Double) o);
                }
                return String.format("%.2f", d);
            } else {
                throw new RuntimeException("Unhandled values of type " + columnData.get(0).getClass().getName());
            }
        }
    }

    public static class ProcessResultColumnTotalAverage extends ProcessResultColumnTotal {

        @Override
        public String getTotal(List<Object> columnData) {
            if (columnData.get(0) == null) {
                throw new RuntimeException("Could not determine type because of null");
            }
            if (columnData.get(0) instanceof Long) {
                long l = 0;
                for (Object o : columnData) {
                    l += (Long) o;
                }
                return String.format("%.2f", (double) l / (double) columnData.size());
            } else if (columnData.get(0) instanceof Double) {
                double d = 0;
                for (Object o : columnData) {
                    d += (Double) o;
                }
                return String.format("%.2f", d / columnData.size());
            } else {
                throw new RuntimeException("Unhandled values of type " + columnData.get(0).getClass().getName());
            }
        }
    }
}
