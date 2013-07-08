package com.altamiracorp.messaging.synchronization;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(JUnit4.class)
public class SynchronizationBusTest {

	private SynchronizationBus bus;

	@Before
	public void setUp() {
		bus = new SynchronizationBus();
	}

	@Test
	public void testInitiateSessionRequest() {
		SynchronizationRequest req = buildRequest();

		SynchronizationRequest newlyRequestedSession = bus
				.initiateSessionRequest(req);
		assertNotNull("Generated session id is null!",
				newlyRequestedSession.getSessionId());
		assertNotNull("Session was not added to the requested collection!",
				bus.getRequestedSession(newlyRequestedSession.getSessionId()));
	}

	@Test(expected = IllegalStateException.class)
	public void testAcceptBadSession() {
		bus.acceptSessionRequest("asdas234234asdfsadf");
	}

	@Test
	public void testRejectSession() {
		SynchronizationRequest req = bus.initiateSessionRequest(buildRequest());
		assertNotNull("Session not returned after reject!",
				bus.rejectSessionRequest(req.getSessionId()));
		assertNull("Session still in the requested collection!",
				bus.getRequestedSession(req.getSessionId()));

		assertNull("Session still exists!",
				bus.rejectSessionRequest(req.getSessionId()).getInitiatorId());

	}

	@Test
	public void testAcceptSession() {
		SynchronizationRequest req = bus.initiateSessionRequest(buildRequest());
		assertNotNull("The request was not accepted!",
				bus.acceptSessionRequest(req.getSessionId()));
		assertNotNull("Session was not added to the active collection!",
				bus.getSession(req.getSessionId()));
	}

	@Test
	public void testEndSession() {
		SynchronizationRequest req = bus.initiateSessionRequest(buildRequest());
		assertNotNull("Session not returned after end!",
				bus.endSession(req.getSessionId()));
		assertNull("Session still in the active collection!",
				bus.getSession(req.getSessionId()));

		assertNull("Session still exists!", bus.endSession(req.getSessionId())
				.getInitiatorId());
	}

	private SynchronizationRequest buildRequest() {
		SynchronizationRequest req = new SynchronizationRequest();
		req.setInitiatorId("ryan");
		List<String> userIds = new ArrayList<String>();
		req.setUserIds(userIds);
		req.getUserIds().add("bob");

		return req;
	}
}
