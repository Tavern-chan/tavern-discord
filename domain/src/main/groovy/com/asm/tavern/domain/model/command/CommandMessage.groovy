package com.asm.tavern.domain.model.command

import groovy.transform.Immutable


@Immutable
class CommandMessage {
	List<Command> commandList
	CommandArgumentUsage usage
	List<String> args
	String message

}
