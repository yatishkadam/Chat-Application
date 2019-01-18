package edu.northeastern.ccs.slack;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.webhook.Payload;

public class Notification {
	private static final Logger logger = LogManager.getLogger(Notification.class);
	
	private Notification() {
	}
	
	public static void sendNotificationToSlack(String message) {
		String url = "https://hooks.slack.com/services/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";

		Payload payload = Payload.builder()
		  .channel("#cs5500-team-207-f18")
		  .username("jSlack Bot")
		  .iconEmoji(":smile_cat:")
		  .text(message)
		  .build();
		Slack slack = Slack.getInstance();
		try {
			slack.send(url, payload);
		} catch (IOException e) {
			logger.error("An error occured while sending message in slack!!" + e);
		}
	}
	

}
