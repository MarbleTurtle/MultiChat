package com.MultiChat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Dual Chat")
public interface MultiChatConfig extends Config{
	@ConfigItem(
			keyName = "send",
			name = "Send Chat",
			description = "Sends chat to other client."
	)
	default boolean send()
	{
		return true;
	}
	@ConfigItem(
			keyName = "receive",
			name = "Receive Chat",
			description = "Receives the chat of the other client."
	)
	default boolean receive()
	{
		return true;
	}
}
