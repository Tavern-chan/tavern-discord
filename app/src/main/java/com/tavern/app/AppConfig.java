package com.tavern.app;

import java.io.File;
import java.util.Properties;

public class AppConfig {
	public AppConfig(Properties properties) {
		this.properties = properties;
	}

	public File getSongFile() {
		return new File(properties.getProperty("songFileLocation"));
	}

	public String getDiscordToken() {
		return properties.getProperty("discordToken");
	}

	public String getSpotifyClientId() {
		return properties.getProperty("spotifyClientId");
	}

	public String getSpotifyClientSecret() {
		return properties.getProperty("spotifyClientSecret");
	}

	public String getPrefix() {
		return properties.getProperty("command.prefix");
	}

	private final Properties properties;
}
