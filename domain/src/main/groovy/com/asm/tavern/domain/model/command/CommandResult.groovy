package com.asm.tavern.domain.model.command

/**
 * Result of the command
 */
interface CommandResult {
	/**
	 * @return true if successful
	 */
	boolean success()

	/**
	 * @return Message to be returned with result
	 */
	String resultMessage()

}