package com.altamiracorp.messaging.synchronization;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterListenerAdapter;
import org.json.JSONException;
import org.json.JSONObject;

public class SynchronizationSessionListener extends BroadcasterListenerAdapter {

	@Override
	public void onRemoveAtmosphereResource(Broadcaster b,
			AtmosphereResource resource) {
		if (SyncUtil.isSyncTopic(b.getID())) {
			String sessionId = SyncUtil.topicToSyncSessionId(b.getID());
			SynchronizationBus bus = SynchronizationBus.getSyncBus(resource
					.getAtmosphereConfig().getServletContext());
			SynchronizationRequest session = bus.getSession(sessionId);
			if (session != null && session.getUserIds().size() == 1) {
				bus.endSession(sessionId);
				JSONObject syncEndMsg = new JSONObject();
				try {
					JSONObject sync = new JSONObject();
					sync.put("initiator", false);
					syncEndMsg.put("sync", sync);
					syncEndMsg.put("syncEnd", true);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				b.broadcast(syncEndMsg.toString());
			}
		}
	}

}
