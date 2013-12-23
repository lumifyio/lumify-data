package com.altamiracorp.lumify.tools;

import com.altamiracorp.lumify.core.cmdline.CommandLineBase;
import com.altamiracorp.lumify.tools.asciitable.ASCIITable;
import com.altamiracorp.lumify.tools.asciitable.ASCIITableHeader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JmxClient extends CommandLineBase {
    private final Object sysoutLock = new Object();
    private Pattern metricRegex = Pattern.compile("metrics:name=(.*)\\.([^.]+)\\.([^.]+)");

    // group, metric name, MetricData
    private final Map<String, Map<String, List<MetricData>>> data = new HashMap<String, Map<String, List<MetricData>>>();

    public static void main(String[] args) throws Exception {
        int res = new JmxClient().run(args);
        System.exit(res);
    }

    public JmxClient() {
        initFramework = false;
    }

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();

        options.addOption(
                OptionBuilder
                        .withLongOpt("ips")
                        .withDescription("IP addresses")
                        .hasArg(true)
                        .withArgName("ips")
                        .create()
        );

        return options;
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        String ipsString = cmd.getOptionValue("ips");
        String[] ips = ipsString.split(",");

        ArrayList<Future<JMXConnector>> connections = new ArrayList<Future<JMXConnector>>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (String ip : ips) {
            connections.add(connectAsync(executor, ip));
        }

        for (Future<JMXConnector> connection : connections) {
            try {
                connection.get(10, TimeUnit.SECONDS);
            } catch (Exception ex) {
                System.out.println("failed to get info: " + ex.getMessage());
            }
        }

        printData();

        System.out.println("DONE");
        return 0;
    }

    private void printData() {
        for (Map.Entry<String, Map<String, List<MetricData>>> groups : data.entrySet()) {
            System.out.println(groups.getKey());
            System.out.println(StringUtils.repeat("-", groups.getKey().length()));

            for (Map.Entry<String, List<MetricData>> metric : groups.getValue().entrySet()) {
                if (metric.getValue().size() > 0) {
                    System.out.println(metric.getKey());

                    ASCIITableHeader[] tableHeaders = metric.getValue().get(0).getTableHeaders();
                    ArrayList<String[]> tableRows = new ArrayList<String[]>();
                    for (MetricData metricData : metric.getValue()) {
                        tableRows.add(metricData.getTableRow());
                    }
                    tableRows.add(metric.getValue().get(0).getTotalRow(metric.getValue()));
                    ASCIITable.printTable(tableHeaders, tableRows);
                    System.out.println();
                }
            }
        }
    }

    private Future<JMXConnector> connectAsync(ExecutorService executor, final String ip) {
        return executor.submit(new Callable<JMXConnector>() {
            public JMXConnector call() {
                try {
                    return connect(ip);
                } catch (Exception ex) {
                    System.out.println("Failed on ip " + ip + ": " + ex.getMessage());
                    return null;
                }
            }
        });
    }

    private JMXConnector connect(String ip) throws IOException, InstanceNotFoundException, IntrospectionException, ReflectionException {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + ip + "/jmxrmi");
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        synchronized (sysoutLock) {
            System.out.println(ip);
            MBeanServerConnection mbeanServerConnection = jmxc.getMBeanServerConnection();
            for (ObjectName mbeanName : mbeanServerConnection.queryNames(null, null)) {
                System.out.println("  found mbean: " + mbeanName.getCanonicalName());
                Matcher m = metricRegex.matcher(mbeanName.getCanonicalName());
                if (m.matches()) {
                    String group = m.group(1);
                    String uniqueId = m.group(2);
                    String metricName = m.group(3);

                    if (uniqueId.contains("-")) {
                        String[] uniqueIdParts = uniqueId.split("-");
                        group = group + "-" + uniqueIdParts[0];
                        uniqueId = uniqueIdParts[1];
                    }

                    List<MetricData> metricDatas;
                    synchronized (data) {
                        Map<String, List<MetricData>> groupData = data.get(group);
                        if (groupData == null) {
                            groupData = new HashMap<String, List<MetricData>>();
                            data.put(group, groupData);
                        }

                        metricDatas = groupData.get(metricName);
                        if (metricDatas == null) {
                            metricDatas = new ArrayList<MetricData>();
                            groupData.put(metricName, metricDatas);
                        }
                    }

                    MBeanInfo info = mbeanServerConnection.getMBeanInfo(mbeanName);
                    ArrayList<String> attributeNames = new ArrayList<String>();
                    for (MBeanAttributeInfo attr : info.getAttributes()) {
                        attributeNames.add(attr.getName());
                    }
                    AttributeList attributes = mbeanServerConnection.getAttributes(mbeanName, attributeNames.toArray(new String[0]));

                    if (attributeNames.size() == 1 && attributeNames.get(0).equals("Count")) {
                        System.out.println("  found counter: " + mbeanName.getCanonicalName());
                        metricDatas.add(new CounterMetricData(ip, group, metricName, uniqueId, attributes));
                    } else if (attributeNames.contains("Min") && attributeNames.contains("Max") && attributeNames.contains("MeanRate")) {
                        System.out.println("  found timer: " + mbeanName.getCanonicalName());
                        metricDatas.add(new TimerMetricData(ip, group, metricName, uniqueId, attributes));
                    } else {
                        System.out.println("Unknown metric data: " + group + ", " + metricName);
                    }
                }
            }
        }
        return jmxc;
    }

    private abstract static class MetricData {
        protected final String ip;
        protected final String group;
        protected final String metricName;
        protected final String uniqueId;

        public MetricData(String ip, String group, String metricName, String uniqueId) {
            this.ip = ip;
            this.group = group;
            this.metricName = metricName;
            this.uniqueId = uniqueId;
        }

        public abstract ASCIITableHeader[] getTableHeaders();

        public abstract String[] getTableRow();

        public abstract String[] getTotalRow(List<MetricData> metricDatas);
    }

    private static class CounterMetricData extends MetricData {
        private final long count;

        public CounterMetricData(String ip, String group, String metricName, String uniqueId, AttributeList attributes) {
            super(ip, group, metricName, uniqueId);
            Attribute attribute = (Attribute) attributes.get(0);
            this.count = (Long) attribute.getValue();
        }

        @Override
        public String toString() {
            return this.ip + "(" + uniqueId + "): Counter: " + this.count;
        }

        @Override
        public ASCIITableHeader[] getTableHeaders() {
            return new ASCIITableHeader[]{
                    new ASCIITableHeader("IP"),
                    new ASCIITableHeader("ID"),
                    new ASCIITableHeader("Count")
            };
        }

        @Override
        public String[] getTableRow() {
            return new String[]{
                    ip,
                    uniqueId,
                    Long.toString(count)
            };
        }

        @Override
        public String[] getTotalRow(List<MetricData> metricDatas) {
            long totalCount = 0;
            for (MetricData genericMetricData : metricDatas) {
                CounterMetricData metricData = (CounterMetricData) genericMetricData;
                totalCount += metricData.count;
            }

            return new String[]{
                    "Total",
                    "",
                    Long.toString(totalCount)
            };
        }
    }

    private static class TimerMetricData extends MetricData {
        private double max;
        private double min;
        private double mean;
        private double stdDev;
        private String durationUnit;
        private long count;
        private double oneMinuteRate;
        private double fiveMinuteRate;
        private double fifteenMinuteRate;
        private double meanRate;
        private String rateUnit;
        private double percentile50th;
        private double percentile75th;
        private double percentile95th;
        private double percentile98th;
        private double percentile99th;
        private double percentile999th;

        public TimerMetricData(String ip, String group, String metricName, String uniqueId, AttributeList attributes) {
            super(ip, group, metricName, uniqueId);
            for (Object attributeObj : attributes) {
                Attribute attribute = (Attribute) attributeObj;
                if ("Max".equalsIgnoreCase(attribute.getName())) {
                    max = (Double) attribute.getValue();
                } else if ("Min".equalsIgnoreCase(attribute.getName())) {
                    min = (Double) attribute.getValue();
                } else if ("Mean".equalsIgnoreCase(attribute.getName())) {
                    mean = (Double) attribute.getValue();
                } else if ("StdDev".equalsIgnoreCase(attribute.getName())) {
                    stdDev = (Double) attribute.getValue();
                } else if ("durationUnit".equalsIgnoreCase(attribute.getName())) {
                    durationUnit = toTimeUnit((String) attribute.getValue());
                } else if ("count".equalsIgnoreCase(attribute.getName())) {
                    count = (Long) attribute.getValue();
                } else if ("fifteenMinuteRate".equalsIgnoreCase(attribute.getName())) {
                    fifteenMinuteRate = (Double) attribute.getValue();
                } else if ("fiveMinuteRate".equalsIgnoreCase(attribute.getName())) {
                    fiveMinuteRate = (Double) attribute.getValue();
                } else if ("meanRate".equalsIgnoreCase(attribute.getName())) {
                    meanRate = (Double) attribute.getValue();
                } else if ("oneMinuteRate".equalsIgnoreCase(attribute.getName())) {
                    oneMinuteRate = (Double) attribute.getValue();
                } else if ("rateUnit".equalsIgnoreCase(attribute.getName())) {
                    rateUnit = toTimeUnit((String) attribute.getValue());
                } else if ("50thpercentile".equalsIgnoreCase(attribute.getName())) {
                    percentile50th = (Double) attribute.getValue();
                } else if ("75thpercentile".equalsIgnoreCase(attribute.getName())) {
                    percentile75th = (Double) attribute.getValue();
                } else if ("95thpercentile".equalsIgnoreCase(attribute.getName())) {
                    percentile95th = (Double) attribute.getValue();
                } else if ("98thpercentile".equalsIgnoreCase(attribute.getName())) {
                    percentile98th = (Double) attribute.getValue();
                } else if ("99thpercentile".equalsIgnoreCase(attribute.getName())) {
                    percentile99th = (Double) attribute.getValue();
                } else if ("999thpercentile".equalsIgnoreCase(attribute.getName())) {
                    percentile999th = (Double) attribute.getValue();
                } else {
                    System.out.println("Unknown attribute for timer: " + attribute.getName());
                }
            }
        }

        private String toTimeUnit(String units) {
            if ("milliseconds".equals(units)) {
                return "ms";
            }
            if ("events/second".equals(units)) {
                return "/s";
            }
            return " " + units;
        }

        @Override
        public String toString() {
            return this.ip + "(" + uniqueId + "): Timer: " + this.meanRate;
        }

        @Override
        public ASCIITableHeader[] getTableHeaders() {
            return new ASCIITableHeader[]{
                    new ASCIITableHeader("IP"),
                    new ASCIITableHeader("ID"),
                    new ASCIITableHeader("Count"),
                    new ASCIITableHeader("Max"),
                    new ASCIITableHeader("Min"),
                    new ASCIITableHeader("Mean"),
                    new ASCIITableHeader("Mean Rate"),
                    new ASCIITableHeader("1min Rate"),
                    new ASCIITableHeader("5min Rate"),
                    new ASCIITableHeader("15min Rate"),
            };
        }

        @Override
        public String[] getTableRow() {
            return new String[]{
                    ip,
                    uniqueId,
                    Long.toString(count),
                    String.format("%.1f%s", max, durationUnit),
                    String.format("%.1f%s", min, durationUnit),
                    String.format("%.1f%s", mean, durationUnit),
                    String.format("%.3f%s", meanRate, rateUnit),
                    String.format("%.3f%s", oneMinuteRate, rateUnit),
                    String.format("%.3f%s", fiveMinuteRate, rateUnit),
                    String.format("%.3f%s", fifteenMinuteRate, rateUnit)
            };
        }

        @Override
        public String[] getTotalRow(List<MetricData> metricDatas) {
            long totalCount = 0;
            double max = 0;
            double min = Double.MAX_VALUE;
            double meanTotal = 0;
            double meanRateTotal = 0;
            double oneMinuteRateTotal = 0;
            double fiveMinuteRateTotal = 0;
            double fifteenMinuteRateTotal = 0;

            for (MetricData genericMetricData : metricDatas) {
                TimerMetricData metricData = (TimerMetricData) genericMetricData;
                totalCount += metricData.count;
                max = Math.max(max, metricData.max);
                min = Math.min(min, metricData.min);
                meanTotal += metricData.mean;
                meanRateTotal += metricData.meanRate;
                oneMinuteRateTotal += metricData.oneMinuteRate;
                fiveMinuteRateTotal += metricData.fiveMinuteRate;
                fifteenMinuteRateTotal += metricData.fifteenMinuteRate;
            }
            double mean = meanTotal / metricDatas.size();
            double meanRate = meanRateTotal / metricDatas.size();
            double oneMinuteRate = oneMinuteRateTotal / metricDatas.size();
            double fiveMinuteRate = fiveMinuteRateTotal / metricDatas.size();
            double fifteenMinuteRate = fifteenMinuteRateTotal / metricDatas.size();

            return new String[]{
                    "Total",
                    "",
                    Long.toString(totalCount),
                    String.format("%.1f%s", max, durationUnit),
                    String.format("%.1f%s", min, durationUnit),
                    String.format("%.1f%s", mean, durationUnit),
                    String.format("%.3f%s", meanRate, rateUnit),
                    String.format("%.3f%s", oneMinuteRate, rateUnit),
                    String.format("%.3f%s", fiveMinuteRate, rateUnit),
                    String.format("%.3f%s", fifteenMinuteRate, rateUnit)
            };
        }
    }
}
