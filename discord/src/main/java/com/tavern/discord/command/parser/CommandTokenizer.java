package com.tavern.discord.command.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CommandTokenizer {

	private final Stack<String> tokens;

	public CommandTokenizer(String message) {
		this.tokens = createMessageStack(message);
	}

	public final Stack<String> getTokens() {
		return tokens;
	}

	private Stack<String> createMessageStack(String message) {
		String[] parts = message.trim().split("\\s+");
		if (parts.length == 0) {
			return new Stack<>();
		}


		Stack<String> tokens = new Stack<>();
		for (int i = parts.length - 1; 0 <= i; i--) {
			String part = parts[i];
			if (!part.isEmpty()) {
				tokens.add(part);
			}
		}

		return tokens;
	}

	public boolean hasNext() {
		return !tokens.empty();
	}

	public String peek() {
		return tokens.peek();
	}

	public String pop() throws IllegalStateException {
		if (!hasNext()) {
			throw new IllegalStateException("No more message tokens in the stack");
		}

		return tokens.pop();
	}

	public List<String> popRemaining() {
		List<String> remainingTokens = new ArrayList<>(tokens);
		tokens.clear();
		return remainingTokens;
	}
}
