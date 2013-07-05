package com.altamiracorp.messaging.synchronization;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class SynchronizationSessionListenerTest {

	private Broadcaster b;
	private AtmosphereResource ar;

	@Before
	public void setUp() {
		AtmosphereConfig ac = Mockito.mock(AtmosphereConfig.class);
		ServletContext ctx = Mockito.mock(ServletContext.class);
		ar = Mockito.mock(AtmosphereResource.class);
		b = Mockito.mock(Broadcaster.class);

		SynchronizationRequest req = new SynchronizationRequest();
		req.setInitiatorId("ryan");
		List<String> userIds = new ArrayList<String>();
		req.setUserIds(userIds);
		req.getUserIds().add("bob");

		SynchronizationBus bus = new SynchronizationBus();
		SynchronizationRequest newReq = bus.initiateSessionRequest(req);
		bus.acceptSessionRequest(newReq.getSessionId());

		when(ctx.getAttribute("SyncBus")).thenReturn(bus);
		when(ar.getAtmosphereConfig()).thenReturn(ac);
		when(ac.getServletContext()).thenReturn(ctx);
		when(b.getID()).thenReturn("sync-" + newReq.getSessionId());
	}

	@Test
	public void testOnAtmosphereResourceRemove() {
		when(b.broadcast(anyObject())).thenAnswer(new Answer<Future<String>>() {
			@Override
			public Future<String> answer(InvocationOnMock invocation)
					throws Throwable {
				Object[] args = invocation.getArguments();
				assertEquals("The syncEnd message was not correct!",
						"{\"syncEnd\":true,\"sync\":{\"initiator\":false}}",
						(String) args[0]);
				return Mockito.mock(Future.class);
			}
		});

		SynchronizationSessionListener listener = new SynchronizationSessionListener();
		listener.onRemoveAtmosphereResource(b, ar);
	}
}
