package com.tavern.domain.model.command;

import com.google.common.collect.Iterables;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

public class CommandHandlerRegistry {
	private final MultiValuedMap<String, CommandHandler> handlers = new ArrayListValuedHashMap<String, CommandHandler>();

	public CommandHandlerRegistry add(CommandHandler commandHandler) {
		this.handlers.put(commandHandler.getCommand().getName().toLowerCase(), commandHandler);
		return this;
	}

	public CommandHandler getHandler(CommandMessage message) {
		return handlers.get(Iterables.getLast(message.getCommandList()).getName()).stream()
			.filter(handler -> handler.supportsUsage(message.getUsage()))
			.findFirst().orElse(null);
	}

}
