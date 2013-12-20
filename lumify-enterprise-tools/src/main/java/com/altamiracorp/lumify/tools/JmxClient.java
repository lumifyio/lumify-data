package com.altamiracorp.lumify.tools;

import com.altamiracorp.lumify.core.cmdline.CommandLineBase;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JmxClient extends CommandLineBase {
    private final Object sysoutLock = new Object();

    public static void main(String[] args) throws Exception {
        int res = new JmxClient().run(args);
        if (res != 0) {
            System.exit(res);
        }
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

        ExecutorService executor = Executors.newSingleThreadExecutor();
        for (String ip : ips) {
            connectAsync(executor, ip);
        }

        return 0;
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
                if (mbeanName.getCanonicalName().startsWith("com.altamiracorp.lumify")) {
                    System.out.println("  " + mbeanName.getCanonicalName());
                    MBeanInfo info = mbeanServerConnection.getMBeanInfo(mbeanName);
                    ArrayList<String> attributeNames = new ArrayList<String>();
                    for (MBeanAttributeInfo attr : info.getAttributes()) {
                        attributeNames.add(attr.getName());
                    }
                    AttributeList attributes = mbeanServerConnection.getAttributes(mbeanName, attributeNames.toArray(new String[0]));
                    for (Attribute attribute : attributes.asList()) {
                        System.out.println("    " + attribute.getName() + " = " + attribute.getValue());
                    }
                }
            }
        }
        return jmxc;
    }
}
