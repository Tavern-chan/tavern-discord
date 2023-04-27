package com.asm.tavern.domain.model.command


class CommandResultBuilder {
	private boolean success
	private String resultMessage = ""

	CommandResultBuilder success() {
		success = true
		this
	}

	CommandResultBuilder error() {
		success = false
		this
	}

	CommandResultBuilder result(String resultMessage) {
		this.resultMessage = resultMessage
		this
	}

	CommandResult build() {
		return new CommandResult() {
			@Override
			boolean success() {
				return success
			}

			@Override
			String resultMessage() {
				return resultMessage
			}
		}
	}

}
