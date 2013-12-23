/*
 * Copyright 2013 Altamira Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.altamiracorp.lumify.core.version;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Properties;

/**
 * This implementation of the LumifyVersionService loads its configuration
 * from classpath://lumify-build.properties.  If the file does not exist,
 * all methods will return <code>null</code>.
 */
@Singleton
public class VersionService implements VersionServiceMXBean {
    public static String JMX_NAME = "com.altamiracorp.lumify:type=" + VersionService.class.getName();

    /**
     * The class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionService.class);

    /**
     * The name of the properties file to read.
     */
    private static final String LUMIFY_BUILD_PROPERTIES = "lumify-build.properties";

    /**
     * The Lumify version property.
     */
    private static final String LUMIFY_VERSION_PROPERTY = "lumify.version";

    /**
     * The SCM build number property.
     */
    private static final String SCM_BUILD_NUMBER_PROPERTY = "lumify.buildNumber";

    /**
     * The build timestamp property.
     */
    private static final String BUILD_TIME_PROPERTY = "lumify.buildTime";

    /**
     * The Lumify version.
     */
    private final String version;

    /**
     * The SCM build number.
     */
    private final String scmBuildNumber;

    /**
     * The build timestamp.
     */
    private final Long unixBuildTime;

    public VersionService() {
        String ver = null;
        String buildNum = null;
        Long buildTime = null;
        try {
            Properties props = new Properties();
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(LUMIFY_BUILD_PROPERTIES);
            if (is == null) {
                throw new FileNotFoundException(String.format("Property file [%s] not found in the classpath.",
                        LUMIFY_BUILD_PROPERTIES));
            }
            props.load(is);
            ver = props.getProperty(LUMIFY_VERSION_PROPERTY);
            buildNum = props.getProperty(SCM_BUILD_NUMBER_PROPERTY);
            String strTime = props.getProperty(BUILD_TIME_PROPERTY);
            if (strTime != null) {
                try {
                    buildTime = Long.parseLong(strTime);
                } catch (NumberFormatException nfe) {
                    LOGGER.warn("Invalid build timestamp [%s].");
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("Unable to read Lumify version properties.", ioe);
        }
        this.version = ver;
        this.scmBuildNumber = buildNum;
        this.unixBuildTime = buildTime;

        try {
            registerJmxBean();
        } catch (Exception ex) {
            LOGGER.error("Could not register JMX bean", ex);
        }
    }

    private void registerJmxBean() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName mxbeanName = new ObjectName(JMX_NAME);
        mbs.registerMBean(this, mxbeanName);
    }

    @Override
    public Long getUnixBuildTime() {
        return unixBuildTime;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getScmBuildNumber() {
        return scmBuildNumber;
    }
}
