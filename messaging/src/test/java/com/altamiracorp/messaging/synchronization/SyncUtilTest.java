package com.altamiracorp.messaging.synchronization;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class SyncUtilTest {

	private String goodTopic = "sync-3242524-4252345-3425";
	private String badTopic = "syncFASadsfadgasfgWER";
	private String sessionId = "3242524-4252345-3425";

	@Test
	public void testIsSyncTopic() {
		assertTrue(SyncUtil.isSyncTopic(goodTopic));
		assertFalse(SyncUtil.isSyncTopic(badTopic));
	}

	@Test
	public void testTopicToSessionId() {
		assertEquals("Session Id is not correct!", sessionId,
				SyncUtil.topicToSyncSessionId(goodTopic));
	}

}
