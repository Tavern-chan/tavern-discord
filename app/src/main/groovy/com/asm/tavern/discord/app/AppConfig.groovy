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

	String getSpotifyClientId() {
		properties.getProperty('spotifyClientId')
	}

	String getSpotifyClientSecret() {
		properties.getProperty('spotifyClientSecret')
	}

	String getPrefix() {
		properties.getProperty('command.prefix')
	}

	String getDiscordClientId() {
		properties.getProperty('discordClientId')
	}

	String getRedirectURI() {
		properties.getProperty('redirectURI')
	}

	String getDiscordClientSecret() {
		properties.getProperty('discordClientSecret')
	}
}
