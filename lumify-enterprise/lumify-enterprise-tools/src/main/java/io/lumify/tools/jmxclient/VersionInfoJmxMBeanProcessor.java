package io.lumify.tools.jmxclient;

import com.altamiracorp.jmxui.JmxMBeanProcessor;
import io.lumify.core.version.VersionService;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VersionInfoJmxMBeanProcessor extends JmxMBeanProcessor {
    @Override
    public ProcessResult process(MBeanServerConnection mbeanServerConnection, String source, ObjectName mbeanName) throws Exception {
        if (!mbeanName.getCanonicalName().equals(VersionService.JMX_NAME)) {
            return null;
        }

        Map<String, Object> attributes = getAttributes(mbeanServerConnection, mbeanName);
        return new ProcessResult("Other", "Version Info", source, getColumns(attributes), false);
    }

    private List<ProcessResultColumn> getColumns(Map<String, Object> attributes) {
        List<ProcessResultColumn> columns = new ArrayList<ProcessResultColumn>();
        columns.add(new ProcessResultColumn("Unix Build Time", attributes.get("UnixBuildTime"), ProcessResultColumnTotal.None));
        columns.add(new ProcessResultColumn("Version", attributes.get("Version"), ProcessResultColumnTotal.None));
        columns.add(new ProcessResultColumn("SCM Build Number", attributes.get("ScmBuildNumber"), ProcessResultColumnTotal.None));
        return columns;
    }
}
