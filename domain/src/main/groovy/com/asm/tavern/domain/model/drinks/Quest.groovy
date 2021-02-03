package com.asm.tavern.domain.model.drinks

interface Quest {

	String getName()

	boolean isComplete()

	/**
	 * Print the quest in a user readable format
	 * @return the formatted quest string
	 */
	String toString()

}