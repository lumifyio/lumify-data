package com.altamiracorp.messaging.users;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;

public class UserUtil {

	public static Pattern USER_TOPIC_PATTERN = Pattern.compile("user-[\\w]+");

	public static Users getLoggedInUsers() {
		Users users = new Users();
		BroadcasterFactory factory = BroadcasterFactory.getDefault();
		Collection<Broadcaster> broadcasters = factory.lookupAll();
		for (Broadcaster b : broadcasters) {
			String userName = topicToUsername(b.getID());
			if (userName != null) {
				User user = new User();
				user.setId(userName);
				user.setDisplayName(userName);
				user.setStatus("online");
				users.getUsers().add(user);
			}
		}

		return users;
	}
	
	public static String topicToUsername (String topic) {
		Matcher matcher = USER_TOPIC_PATTERN.matcher(topic);
		if (matcher.find()) {
			return matcher.group().split("-")[1];
		} else {
			return null;
		}
	}
	
	public static boolean isUserTopic (String topic) {
		Matcher matcher = USER_TOPIC_PATTERN.matcher(topic);
		return matcher.matches();
	}
}
