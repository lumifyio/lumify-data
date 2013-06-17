package com.altamiracorp.messaging;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.atmosphere.annotation.Broadcast;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.jersey.Broadcastable;
import org.atmosphere.jersey.SuspendResponse;

import com.altamiracorp.messaging.synchronization.SynchronizationSessionListener;
import com.altamiracorp.messaging.users.UserChangeListener;

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
			BroadcasterFactory.getDefault().addBroadcasterListener(
					userChangeListener);
		} else if (topic.getID().startsWith("sync-")
				&& syncSessionListener == null) {
			syncSessionListener = new SynchronizationSessionListener();
			BroadcasterFactory.getDefault().addBroadcasterListener(
					syncSessionListener);
		}
		
		return new SuspendResponse.SuspendResponseBuilder<String>()
				.broadcaster(topic).outputComments(true).build();
	}

	@POST
	@Broadcast
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("text/html;charset=ISO-8859-1")
	public Broadcastable publish(String message) {
		return new Broadcastable(message, "", topic);
	}
}