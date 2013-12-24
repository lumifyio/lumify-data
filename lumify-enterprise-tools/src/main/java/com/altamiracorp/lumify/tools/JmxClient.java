package com.altamiracorp.lumify.tools;

import com.altamiracorp.lumify.core.cmdline.CommandLineBase;
import com.altamiracorp.lumify.tools.asciitable.ASCIITable;
import com.altamiracorp.lumify.tools.asciitable.ASCIITableHeader;
import com.altamiracorp.lumify.tools.jmxclient.JmxMBeanProcessor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JmxClient extends CommandLineBase {
    private List<JmxMBeanProcessor> jmxMBeanProcessors = new ArrayList<JmxMBeanProcessor>();

    // group, metric name, results
    private final Map<String, Map<String, List<JmxMBeanProcessor.ProcessResult>>> results = new HashMap<String, Map<String, List<JmxMBeanProcessor.ProcessResult>>>();

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

        for (JmxMBeanProcessor s : ServiceLoader.load(JmxMBeanProcessor.class)) {
            jmxMBeanProcessors.add(s);
        }

        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (String ip : ips) {
            connectAsync(executor, ip);
        }
        executor.shutdown();

        long deadline = System.currentTimeMillis() + 30000;
        while (!executor.isTerminated() && System.currentTimeMillis() < deadline) {
            Thread.sleep(100);
        }

        System.out.println();
        System.out.println();
        System.out.println();
        printResults();

        System.out.println("DONE");
        return 0;
    }

    private void printResults() {
        for (Map.Entry<String, Map<String, List<JmxMBeanProcessor.ProcessResult>>> groupResults : results.entrySet()) {
            printGroupResults(groupResults.getKey(), groupResults.getValue());
        }
    }

    private void printGroupResults(String groupName, Map<String, List<JmxMBeanProcessor.ProcessResult>> groupResults) {
        System.out.println(groupName);
        System.out.println(StringUtils.repeat("=", groupName.length()));
        for (Map.Entry<String, List<JmxMBeanProcessor.ProcessResult>> metricResults : groupResults.entrySet()) {
            printMetricResults(metricResults.getKey(), metricResults.getValue());
        }
    }

    private void printMetricResults(String metricName, List<JmxMBeanProcessor.ProcessResult> metricResults) {
        System.out.println(metricName);
        try {
            if (metricResults.size() == 0) {
                System.out.println("   NONE");
                return;
            }

            ASCIITableHeader[] tableHeaders = columnsToASCIITableHeaders(metricResults.get(0).getColumns());
            ArrayList<String[]> tableRows = new ArrayList<String[]>();
            for (JmxMBeanProcessor.ProcessResult result : metricResults) {
                tableRows.add(resultToTableRow(result));
            }
            if (metricResults.get(0).isHasTotalLine()) {
                tableRows.add(resultsToTotalRow(metricResults));
            }
            ASCIITable.printTable(tableHeaders, tableRows);
        } catch (Exception ex) {
            System.out.println("  Could not print table:");
            ex.printStackTrace(System.out);
        }
    }

    private String[] resultToTableRow(JmxMBeanProcessor.ProcessResult result) {
        String[] columns = new String[result.getColumns().size() + 1];
        for (int i = 0; i < columns.length; i++) {
            if (i == 0) {
                columns[i] = result.getSource();
            } else {
                columns[i] = result.getColumns().get(i - 1).toString();
            }
        }
        return columns;
    }

    private String[] resultsToTotalRow(List<JmxMBeanProcessor.ProcessResult> metricResults) {
        String[] columns = new String[metricResults.get(0).getColumns().size() + 1];
        for (int i = 0; i < columns.length; i++) {
            if (i == 0) {
                columns[i] = "Total";
            } else {
                JmxMBeanProcessor.ProcessResultColumnTotal total = metricResults.get(0).getColumns().get(i - 1).getTotal();
                columns[i] = total.getTotal(getColumnData(metricResults, i - 1));
            }
        }
        return columns;
    }

    private List<Object> getColumnData(List<JmxMBeanProcessor.ProcessResult> metricResults, int columnNumber) {
        List<Object> data = new ArrayList<Object>();
        for (JmxMBeanProcessor.ProcessResult r : metricResults) {
            data.add(r.getColumns().get(columnNumber).getValue());
        }
        return data;
    }

    private ASCIITableHeader[] columnsToASCIITableHeaders(List<JmxMBeanProcessor.ProcessResultColumn> columns) {
        List<ASCIITableHeader> headers = new ArrayList<ASCIITableHeader>();
        headers.add(new ASCIITableHeader("Source"));
        for (JmxMBeanProcessor.ProcessResultColumn column : columns) {
            headers.add(columnsToASCIITableHeader(column));
        }
        return headers.toArray(new ASCIITableHeader[0]);
    }

    private ASCIITableHeader columnsToASCIITableHeader(JmxMBeanProcessor.ProcessResultColumn column) {
        return new ASCIITableHeader(column.getName(), column.getAlignment());
    }

    private void connectAsync(ExecutorService executor, final String ip) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    connect(ip);
                } catch (Exception ex) {
                    System.err.println("Failed on ip " + ip + ":");
                    ex.printStackTrace(System.err);
                }
            }
        });
    }

    private JMXConnector connect(String ip) throws IOException, InstanceNotFoundException, IntrospectionException, ReflectionException {
        System.err.println("trying: " + ip);
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + ip + "/jmxrmi");
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbeanServerConnection = jmxc.getMBeanServerConnection();
        for (ObjectName mbeanName : mbeanServerConnection.queryNames(null, null)) {
            for (JmxMBeanProcessor jmxMBeanProcessor : jmxMBeanProcessors) {
                try {
                    JmxMBeanProcessor.ProcessResult result = jmxMBeanProcessor.process(mbeanServerConnection, ip, mbeanName);
                    if (result != null) {
                        synchronized (results) {
                            Map<String, List<JmxMBeanProcessor.ProcessResult>> groupResults = results.get(result.getGroup());
                            if (groupResults == null) {
                                groupResults = new HashMap<String, List<JmxMBeanProcessor.ProcessResult>>();
                                results.put(result.getGroup(), groupResults);
                            }
                            List<JmxMBeanProcessor.ProcessResult> namedResults = groupResults.get(result.getName());
                            if (namedResults == null) {
                                namedResults = new ArrayList<JmxMBeanProcessor.ProcessResult>();
                                groupResults.put(result.getName(), namedResults);
                            }
                            namedResults.add(result);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Could not process mbean: " + mbeanName);
                    ex.printStackTrace(System.err);
                }
            }
        }
        return jmxc;
    }
}
