package com.altamiracorp.messaging.synchronization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyncUtil {
	
	private static final Pattern SYNC_TOPIC_PATTERN = Pattern.compile("sync-[\\w\\-]+");
	
	public static String topicToSyncSessionId(String topic) {
		return topic.substring(topic.indexOf("-") + 1);
	}

	public static boolean isSyncTopic(String topic) {
		Matcher matcher = SYNC_TOPIC_PATTERN.matcher(topic);
		return matcher.matches();
	}

}
