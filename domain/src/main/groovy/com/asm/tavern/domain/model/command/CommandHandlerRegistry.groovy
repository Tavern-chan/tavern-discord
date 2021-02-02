package com.asm.tavern.domain.model.command

import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap


class CommandHandlerRegistry {
	final MultiValuedMap<String, CommandHandler> handlers = new ArrayListValuedHashMap<>()

	CommandHandlerRegistry add(CommandHandler commandHandler) {
		this.handlers.put(commandHandler.command.name.toLowerCase(), commandHandler)
		this
	}

	CommandHandler getHandler(CommandMessage message) {
		handlers.get(message.commandList.last().name).stream()
				.filter(handler -> handler.supportsUsage(message.usage))
				.findFirst().orElse(null)
	}

}
