package com.altamiracorp.messaging.users;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.BroadcasterListener;
import org.json.JSONException;

public class UserChangeListener implements BroadcasterListener {

	private static final String USER_CHANGE_TOPIC = "userChanges";

	public void onAddAtmosphereResource(Broadcaster b,
			AtmosphereResource resource) {
		// no-op
	}

	public void onComplete(Broadcaster b) {
		// no-op
	}

	public void onPostCreate(Broadcaster b) {
		if (UserUtil.isUserTopic(b.getID())) {
			UserUtil.changeUserStatus(UserUtil.createUserFromTopic(b.getID()), "online");
			try {
				BroadcasterFactory.getDefault().lookup(USER_CHANGE_TOPIC)
						.broadcast(UserUtil.getSessionUsers().toJson());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void onPreDestroy(Broadcaster b) {
		if (UserUtil.isUserTopic(b.getID())) {
			try {
				UserUtil.changeUserStatus(UserUtil.createUserFromTopic(b.getID()), "offline");
				BroadcasterFactory.getDefault().lookup(USER_CHANGE_TOPIC, true)
						.broadcast(UserUtil.getSessionUsers().toJson());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void onRemoveAtmosphereResource(Broadcaster b,
			AtmosphereResource resource) {
		// no-op
	}

}
