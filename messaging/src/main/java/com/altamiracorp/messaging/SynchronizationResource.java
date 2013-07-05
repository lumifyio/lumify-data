package com.altamiracorp.messaging;

import com.altamiracorp.messaging.synchronization.SynchronizationBus;
import com.altamiracorp.messaging.synchronization.SynchronizationRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

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
