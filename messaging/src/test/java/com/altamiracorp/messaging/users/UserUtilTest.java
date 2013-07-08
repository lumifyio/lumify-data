package com.altamiracorp.messaging.users;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class UserUtilTest {

	private String goodTopicName = "user-rlanman";
	private String badTopicName = "userChanges";

	@Test
	public void testIsUserTopic() {
		assertTrue("Topic should have been a proper user topic!",
				UserUtil.isUserTopic(goodTopicName));
		assertFalse("Topic should not have been a proper user topic!",
				UserUtil.isUserTopic(badTopicName));
	}

	@Test
	public void testTopicToUsername() {
		assertEquals("User name did not return correctly!", "rlanman",
				UserUtil.topicToUsername(goodTopicName));
	}

	@Test
	public void testUserFromTopic() {
		User user = UserUtil.createUserFromTopic(goodTopicName);
		assertEquals("User did not return correctly!", "rlanman", user.getId());
	}

	@Test
	public void testChangeUserStatus() {
		User user = UserUtil.createUserFromTopic(goodTopicName);
		UserUtil.changeUserStatus(user, "online");
		assertEquals("User's status should be online!", "online",
				user.getStatus());

		UserUtil.changeUserStatus(user, "offline");
		assertEquals("Usert's status should be offline!", "offline",
				user.getStatus());
	}

	@Test
	public void testGetSessionUsers() {
		User user = UserUtil.createUserFromTopic(goodTopicName);
		UserUtil.changeUserStatus(user, "online");
		Users users = UserUtil.getSessionUsers();
		assertEquals("First session user should be rlanman!", "rlanman", users
				.getUsers().get(0).getId());
		assertEquals("First session user should be online!", "online", users
				.getUsers().get(0).getStatus());

		UserUtil.changeUserStatus(user, "offline");
		assertEquals("First session user should be offline!", "offline", users
				.getUsers().get(0).getStatus());
	}
}
