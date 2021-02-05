package com.asm.tavern.discord.app

class AppConfig {
	private final Properties properties

	AppConfig(Properties properties) {
		this.properties = properties
	}

	File getSongFile() {
		new File(properties.getProperty('songFileLocation'))
	}

	String getDiscordToken() {
		properties.getProperty('discordToken')
	}

	String getPrefix() {
		properties.getProperty('command.prefix')
	}

}
