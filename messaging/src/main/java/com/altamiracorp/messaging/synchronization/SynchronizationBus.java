package com.altamiracorp.messaging.synchronization;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class SynchronizationBus {

	private static final String SYNC_BUS_ATTRIBUTE = "SyncBus";

	private ConcurrentHashMap<String, SynchronizationRequest> requestedSessions = new ConcurrentHashMap<String, SynchronizationRequest>();

	private ConcurrentHashMap<String, SynchronizationRequest> activeSessions = new ConcurrentHashMap<String, SynchronizationRequest>();

	public static SynchronizationBus getSyncBus(HttpServletRequest req) {
		return getSyncBus(req.getSession().getServletContext());
	}

	public static SynchronizationBus getSyncBus(ServletContext ctx) {
		SynchronizationBus bus = (SynchronizationBus) ctx
				.getAttribute(SYNC_BUS_ATTRIBUTE);
		if (bus == null) {
			bus = new SynchronizationBus();
			ctx.setAttribute(SYNC_BUS_ATTRIBUTE, bus);
		}

		return bus;
	}

	public SynchronizationRequest initiateSessionRequest(
			SynchronizationRequest request) {
		String id = UUID.randomUUID().toString();
		request.setSessionId(id);

		requestedSessions.put(id, request);

		return request;
	}

	public SynchronizationRequest acceptSessionRequest(String id) {
		SynchronizationRequest request = requestedSessions.remove(id);
		if (request == null) {
			throw new IllegalStateException(
					"No synchronization session request associated with id: "
							+ id);
		}
		activeSessions.put(id, request);

		return request;
	}

	public SynchronizationRequest rejectSessionRequest(String id) {
		SynchronizationRequest syncRequest = requestedSessions.remove(id);
		if (syncRequest == null) {
			syncRequest = new SynchronizationRequest();
			syncRequest.setUserIds(new ArrayList<String>());
		}
		return syncRequest;
	}
	
	public SynchronizationRequest endSession(String id) {
		SynchronizationRequest syncRequest = activeSessions.remove(id);
		if (syncRequest == null) {
			syncRequest = new SynchronizationRequest();
			syncRequest.setUserIds(new ArrayList<String>());
		}
		return syncRequest;
	}
	
	public SynchronizationRequest getSession (String id) {
		return this.activeSessions.get(id);
	}
	
	public SynchronizationRequest getRequestedSession (String id) {
		return this.requestedSessions.get(id);
	}

}
