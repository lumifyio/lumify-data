package com.altamiracorp.messaging;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.atmosphere.cpr.BroadcasterFactory;

import com.altamiracorp.messaging.users.UserChangeListener;

public class StartupListener implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent evt) {
		// no-op
	}

	public void contextInitialized(ServletContextEvent evt) {
		BroadcasterFactory bFactory = BroadcasterFactory.getDefault();
		bFactory.addBroadcasterListener(new UserChangeListener());
	}

}
