package io.lumify.tools.jmxclient;

import com.altamiracorp.jmxui.JmxMBeanProcessor;
import org.apache.commons.lang.StringUtils;

import javax.management.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LumifyMetricsJmxMBeanProcessor extends JmxMBeanProcessor {
    private Pattern metricRegex = Pattern.compile("metrics:name=(.*)\\.([^.]+)\\.([^.]+)");

    @Override
    public ProcessResult process(MBeanServerConnection mbeanServerConnection, String source, ObjectName mbeanName) throws IntrospectionException, ReflectionException, InstanceNotFoundException, IOException {
        Matcher m = metricRegex.matcher(mbeanName.getCanonicalName());
        if (!m.matches()) {
            return null;
        }

        String group = m.group(1);
        String uniqueId = m.group(2);
        String metricName = m.group(3);

        if (uniqueId.contains("-")) {
            String[] uniqueIdParts = uniqueId.split("-");
            group = group + "-" + uniqueIdParts[0];
            uniqueId = uniqueIdParts[1];
        }

        Map<String, Object> attributes = getAttributes(mbeanServerConnection, mbeanName);

        if (attributes.size() == 1 && attributes.containsKey("Count")) {
            return new ProcessResult(group, metricName, source, getColumnsForCounter(uniqueId, attributes), true);
        } else if (attributes.containsKey("Min") && attributes.containsKey("Max") && attributes.containsKey("MeanRate")) {
            return new ProcessResult(group, metricName, source, getColumnsForTimer(uniqueId, attributes), true);
        } else if (attributes.size() == 1 && attributes.containsKey("Value")) {
            return new ProcessResult(group, metricName, source, getColumnsForGauge(uniqueId, attributes), true);
        } else {
            throw new RuntimeException("Unknown metric data: [group: " + group + ", metricName: " + metricName + "]: " + StringUtils.join(attributes.keySet(), ","));
        }
    }

    private List<ProcessResultColumn> getColumnsForGauge(String uniqueId, Map<String, Object> attributes) {
        List<ProcessResultColumn> columns = new ArrayList<ProcessResultColumn>();
        columns.add(new ProcessResultColumn("ID", uniqueId, ProcessResultColumnTotal.None));
        columns.add(new ProcessResultColumn("Value", attributes.get("Value"), ProcessResultColumnTotal.Sum));
        return columns;
    }

    private List<ProcessResultColumn> getColumnsForCounter(String uniqueId, Map<String, Object> attributes) {
        List<ProcessResultColumn> columns = new ArrayList<ProcessResultColumn>();
        columns.add(new ProcessResultColumn("ID", uniqueId, ProcessResultColumnTotal.None));
        columns.add(new ProcessResultColumn("Count", attributes.get("Count"), ProcessResultColumnTotal.Sum));
        return columns;
    }

    private List<ProcessResultColumn> getColumnsForTimer(String uniqueId, Map<String, Object> attributes) {
        List<ProcessResultColumn> columns = new ArrayList<ProcessResultColumn>();

        columns.add(new ProcessResultColumn("ID", uniqueId, ProcessResultColumnTotal.None));
        columns.add(new ProcessResultColumn("Count", attributes.get("Count"), ProcessResultColumnTotal.Sum));
        columns.add(new ProcessResultColumn("Max", attributes.get("Max"), ProcessResultColumnTotal.Max));
        columns.add(new ProcessResultColumn("Min", attributes.get("Min"), ProcessResultColumnTotal.Min));
        columns.add(new ProcessResultColumn("Mean", attributes.get("Mean"), ProcessResultColumnTotal.Average));
        columns.add(new ProcessResultColumn("50%", attributes.get("50thPercentile"), ProcessResultColumnTotal.Average));
        columns.add(new ProcessResultColumn("75%", attributes.get("75thPercentile"), ProcessResultColumnTotal.Average));
        columns.add(new ProcessResultColumn("95%", attributes.get("95thPercentile"), ProcessResultColumnTotal.Average));
        columns.add(new ProcessResultColumn("Duration Units", attributes.get("DurationUnit"), ProcessResultColumnTotal.First));
        columns.add(new ProcessResultColumn("Mean Rate", attributes.get("MeanRate"), ProcessResultColumnTotal.Sum));
        columns.add(new ProcessResultColumn("1min Rate", attributes.get("OneMinuteRate"), ProcessResultColumnTotal.Sum));
        columns.add(new ProcessResultColumn("5min Rate", attributes.get("FiveMinuteRate"), ProcessResultColumnTotal.Sum));
        columns.add(new ProcessResultColumn("15min Rate", attributes.get("FifteenMinuteRate"), ProcessResultColumnTotal.Sum));
        columns.add(new ProcessResultColumn("Rate Units", attributes.get("RateUnit"), ProcessResultColumnTotal.First));
        return columns;
    }
}
