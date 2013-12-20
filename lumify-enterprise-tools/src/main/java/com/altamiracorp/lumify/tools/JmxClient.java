package com.altamiracorp.lumify.tools;

import com.altamiracorp.lumify.core.cmdline.CommandLineBase;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.ArrayList;

public class JmxClient extends CommandLineBase {
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

        for (String ip : ips) {
            System.out.println(ip);
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + ip + "/jmxrmi");
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
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

        return 0;
    }
}
