package com.relayrides.pushy.apns;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.atomic.AtomicInteger;

import com.relayrides.pushy.apns.ApnsException;
import com.relayrides.pushy.util.SimpleApnsPushNotification;

public class MockApnsServerHandler extends SimpleChannelInboundHandler<ReceivedApnsPushNotification<SimpleApnsPushNotification>> {

	private final MockApnsServer server;
	private final AtomicInteger receivedNotificationCount;
	
	public MockApnsServerHandler(final MockApnsServer server) {
		this.server = server;
		this.receivedNotificationCount = new AtomicInteger(0);
	}
	
	@Override
	protected void channelRead0(final ChannelHandlerContext context, ReceivedApnsPushNotification<SimpleApnsPushNotification> notification) throws Exception {
		final int receivedNotifications = this.receivedNotificationCount.incrementAndGet();
		
		if (this.server.shouldCloseSilentlyForNotificationCount(receivedNotifications)) {
			context.close();
		} else if (this.server.getErrorCodeForReceivedNotificationCount(receivedNotifications) != null) {
			context.write(new ApnsException(
					notification.getNotificationId(),
					this.server.getErrorCodeForReceivedNotificationCount(receivedNotifications)));
			
			context.close();
		} else {
			this.server.addReceivedNotification(notification.getPushNotification());
		}
	}
}