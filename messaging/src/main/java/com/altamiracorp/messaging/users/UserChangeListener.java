package com.altamiracorp.messaging.users;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.BroadcasterListenerAdapter;
import org.json.JSONException;

public class UserChangeListener extends BroadcasterListenerAdapter {

	private static final String USER_CHANGE_TOPIC = "userChanges";

	@Override
	public void onPostCreate(Broadcaster b) {
		if (UserUtil.isUserTopic(b.getID())) {
			UserUtil.changeUserStatus(UserUtil.createUserFromTopic(b.getID()),
					"online");
			try {
				BroadcasterFactory.getDefault().lookup(USER_CHANGE_TOPIC)
						.broadcast(UserUtil.getSessionUsers().toJson());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onPreDestroy(Broadcaster b) {
		if (UserUtil.isUserTopic(b.getID())) {
			try {
				UserUtil.changeUserStatus(
						UserUtil.createUserFromTopic(b.getID()), "offline");
				BroadcasterFactory.getDefault().lookup(USER_CHANGE_TOPIC, true)
						.broadcast(UserUtil.getSessionUsers().toJson());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
