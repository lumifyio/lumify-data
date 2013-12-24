package com.altamiracorp.lumify.tools.jmxclient;

import com.altamiracorp.lumify.tools.util.TimePeriod;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JvmJmxMBeanProcessor extends JmxMBeanProcessor {
    @Override
    public ProcessResult process(MBeanServerConnection mbeanServerConnection, String source, ObjectName mbeanName) throws Exception {
        if (!mbeanName.getCanonicalName().startsWith("java.lang")) {
            return null;
        }

        if (mbeanName.getCanonicalName().contains("type=Runtime")) {
            Map<String, Object> attributes = getAttributes(mbeanServerConnection, mbeanName);
            return new ProcessResult("JVM", "Runtime", source, runtimeToColumns(attributes), false);
        }

        return null;
    }

    private List<ProcessResultColumn> runtimeToColumns(Map<String, Object> attributes) {
        List<ProcessResultColumn> columns = new ArrayList<ProcessResultColumn>();
        columns.add(new ProcessResultColumn("Uptime (min)", new TimePeriod((Long) attributes.get("Uptime")), ProcessResultColumnTotal.None));
        columns.add(new ProcessResultColumn("Start Time", new Date((Long) attributes.get("StartTime")), ProcessResultColumnTotal.None));
        return columns;
    }
}
