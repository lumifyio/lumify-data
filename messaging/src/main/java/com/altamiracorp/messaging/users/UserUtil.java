package com.altamiracorp.messaging.users;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;

public class UserUtil {

	public static Pattern USER_TOPIC_PATTERN = Pattern.compile("user-[\\w]+");

	private static ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();

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
	
	public static User createUserFromTopic (String topic) {
		String userName = topicToUsername(topic);
		User user = new User();
		user.setId(userName);
		user.setDisplayName(userName);
		
		return user;
	}

	public static void changeUserStatus(User user, String status) {
		if (users.contains(user.getId())) {
			users.get(user.getId()).setStatus(status);
		} else {
			user.setStatus(status);
			users.put(user.getId(),user);
		}
	}
	
	public static Users getSessionUsers () {
		Users sessionUsers = new Users ();
		for (User u : users.values()) {
			sessionUsers.getUsers().add(u);
		}
		
		return sessionUsers;
	}

	public static String topicToUsername(String topic) {
		return topic.split("-")[1];
	}

	public static boolean isUserTopic(String topic) {
		Matcher matcher = USER_TOPIC_PATTERN.matcher(topic);
		return matcher.matches();
	}
}
