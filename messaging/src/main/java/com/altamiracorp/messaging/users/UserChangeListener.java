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
		System.out.println("Topic created! " + b.getID());
		if (UserUtil.isUserTopic(b.getID())) {
			try {
				BroadcasterFactory.getDefault().lookup(USER_CHANGE_TOPIC).broadcast(
						UserUtil.getLoggedInUsers().toJson());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void onPreDestroy(Broadcaster b) {
		//String userName = UserUtil.topicToUsername(b.getID());
		if (UserUtil.isUserTopic(b.getID())) {
			/*Users loggedInUsers = UserUtil.getLoggedInUsers();
			CollectionUtils.filter(loggedInUsers.getUsers(), PredicateUtils
					.notPredicate(PredicateUtils.equalPredicate(userName)));*/

			try {
				BroadcasterFactory.getDefault().lookup(USER_CHANGE_TOPIC).broadcast(
						UserUtil.getLoggedInUsers().toJson());
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
