package com.altamiracorp.lumify.tools.jmxclient;

import com.altamiracorp.jmxui.JmxMBeanProcessor;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KafkaLogJmxMBeanProcessor extends JmxMBeanProcessor {
    private Pattern kafkaLogRegex = Pattern.compile("kafka:type=kafka.logs.(.*)");

    @Override
    public ProcessResult process(MBeanServerConnection mbeanServerConnection, String source, ObjectName mbeanName) throws Exception {
        Matcher m = kafkaLogRegex.matcher(mbeanName.getCanonicalName());
        if (!m.matches()) {
            return null;
        }
        String logName = m.group(1);
        Map<String, Object> attributes = getAttributes(mbeanServerConnection, mbeanName);
        return new ProcessResult("Other", "Kafka", source, getColumns(logName, attributes), false);
    }

    private List<ProcessResultColumn> getColumns(String logName, Map<String, Object> attributes) {
        List<ProcessResultColumn> columns = new ArrayList<ProcessResultColumn>();

        long currentOffset = (Long) attributes.get("CurrentOffset");
        long size = (Long) attributes.get("Size");
        double percentComplete = ((double) currentOffset / (double) size) * 100.0;

        columns.add(new ProcessResultColumn("Log Name", logName, ProcessResultColumnTotal.None, ProcessResultColumn.ALIGN_LEFT));
        columns.add(new ProcessResultColumn("Current Offset (B)", currentOffset, ProcessResultColumnTotal.None));
        columns.add(new ProcessResultColumn("Size (B)", size, ProcessResultColumnTotal.None));
        columns.add(new ProcessResultColumn("Lag (B)", size - currentOffset, ProcessResultColumnTotal.None));
        columns.add(new ProcessResultColumn("Percent Complete", String.format(".2f%", percentComplete), ProcessResultColumnTotal.None));
        return columns;
    }
}
