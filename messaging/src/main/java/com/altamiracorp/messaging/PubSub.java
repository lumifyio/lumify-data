package com.altamiracorp.messaging;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.atmosphere.annotation.Broadcast;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.jersey.Broadcastable;
import org.atmosphere.jersey.SuspendResponse;

import com.altamiracorp.messaging.synchronization.SynchronizationSessionListener;
import com.altamiracorp.messaging.users.UserChangeListener;

/**
 * Simple PubSub resource that demonstrate many functionality supported by
 * Atmosphere JQuery Plugin and Atmosphere Jersey extension.
 * 
 * @author Jeanfrancois Arcand
 */
@Path("/pubsub/{topic}")
public class PubSub {
	
	private static UserChangeListener userChangeListener;
	private static SynchronizationSessionListener syncSessionListener;

	private @PathParam("topic")
	Broadcaster topic;

	@GET
	public SuspendResponse<String> subscribe() {
		if (topic.getID().equals("userChanges") && userChangeListener == null) {
			userChangeListener = new UserChangeListener();
			BroadcasterFactory.getDefault().addBroadcasterListener(userChangeListener);
		} else if (topic.getID().startsWith("sync-") && syncSessionListener == null) {
			syncSessionListener = new SynchronizationSessionListener();
			BroadcasterFactory.getDefault().addBroadcasterListener(syncSessionListener);
		}
		return new SuspendResponse.SuspendResponseBuilder<String>()
				.broadcaster(topic).outputComments(true).build();
	}

	@POST
	@Broadcast
	@Produces("text/html;charset=ISO-8859-1")
	public Broadcastable publish(@FormParam("message") String message) {
		return new Broadcastable(message, "", topic);
	}
}