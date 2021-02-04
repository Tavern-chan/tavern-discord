package com.asm.tavern.domain.model.command

import groovy.transform.Immutable

import java.util.stream.Collectors


@Immutable
class CommandMessage {
	List<Command> commandList
	CommandArgumentUsage usage
	List<String> args
	String message

	String getCommandString() {
		String.join(" ", commandList.stream().map({c -> c.name}).collect(Collectors.toList()))
	}

}
