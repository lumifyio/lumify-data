package com.altamiracorp.messaging;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;

import com.altamiracorp.messaging.synchronization.SynchronizationBus;
import com.altamiracorp.messaging.synchronization.SynchronizationRequest;

@Path("/sync")
public class SynchronizationResource {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public SynchronizationRequest initiateSyncRequest(
			@Context HttpServletRequest req, SynchronizationRequest syncRequest) {
		SynchronizationBus bus = SynchronizationBus.getSyncBus(req);

		// id is set, so just return the request
		bus.initiateSessionRequest(syncRequest);

		return syncRequest;
	}

	@Path("{id}/accept")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public SynchronizationRequest acceptSyncRequest(
			@Context HttpServletRequest req, @PathParam("id") String sessionId) {
		SynchronizationBus bus = SynchronizationBus.getSyncBus(req);

		return bus.acceptSessionRequest(sessionId);
	}

	@Path("{id}/reject")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public SynchronizationRequest rejectSyncRequest(
			@Context HttpServletRequest req, @PathParam("id") String sessionId) {
		SynchronizationBus bus = SynchronizationBus.getSyncBus(req);
		return bus.rejectSessionRequest(sessionId);
	}

	@Path("{id}/end")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public SynchronizationRequest endSyncRequest(
			@Context HttpServletRequest req, @PathParam("id") String sessionId) {
		SynchronizationBus bus = SynchronizationBus.getSyncBus(req);
		SynchronizationRequest syncRequest = bus.endSession(sessionId);
		return syncRequest;
	}
}
