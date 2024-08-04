package com.tavern.domain.model;

import com.tavern.domain.model.command.Command;
import com.tavern.domain.model.command.CommandArgumentUsage;
import com.tavern.domain.model.command.CommandFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TavernCommands {
	public static class Roles {
		public static final String DJ = "DJ";
	}

	public static class Tags {
		public static final String GENERAL = "General";
		public static final String MUSIC = "Music";
		public static final String DRINKING = "Drinking";
	}

	public static class HelpUsages {
		public static final CommandArgumentUsage DEFAULT = CommandFactory.usage("default", "Display the help menu").build();
		public static final CommandArgumentUsage COMMAND_HELP = CommandFactory.usage("command", "Get help with a command or sub command").varArgs().add(CommandFactory.argument("command", "The command to get help with").example("songs")).add(CommandFactory.argument("subCommand", "One or more sub commands").example("add")).build();
	}

	public static class RollUsages {
		public static final CommandArgumentUsage DEFAULT = CommandFactory.usage("default", "Roll a 6 sided die").build();
		public static final CommandArgumentUsage SIDES = CommandFactory.usage("sides", "Roll a die with a given amount of sides").add(CommandFactory.argument("sides", "Amount of sides on the die").example("6")).build();
		public static final CommandArgumentUsage AMOUNT_AND_SIDES = CommandFactory.usage("amount and sides", "Roll a number of dice with a given amount of sides").add(CommandFactory.argument("dice", "Amount of dice").example("1")).add(CommandFactory.argument("sides", "Amount of sides on the die").example("6")).build();
		public static final CommandArgumentUsage AMOUNT_X_SIDES = CommandFactory.usage("amount x sides", "Roll a number or dice with a given amount of sides").add(CommandFactory.argument("amount x sides", "amount of dice and sides per die").example("1x6")).build();
		public static final CommandArgumentUsage ROLL_TIDE = CommandFactory.usage("?", "?").add(CommandFactory.argument("?", "?")).build();
	}

	public static class SongsUsages {
		public static final CommandArgumentUsage DEFAULT = CommandFactory.usage("default", "List songs").build();
		public static final CommandArgumentUsage ADD_WITH_CATEGORY = CommandFactory.usage("add ", "Register a new song with category").add(CommandFactory.argument("add", "required").example("add")).add(CommandFactory.argument("id", "The id to register the song as").example("chuchu")).add(CommandFactory.argument("url", "The url of the song to register").example("https://www.youtube.com/watch?v=5d32-RnUlAA")).add(CommandFactory.argument("category", "The category of the song to register").example("powerhour")).requireRole(Roles.DJ).build();
		public static final CommandArgumentUsage ADD = CommandFactory.usage("add", "Register a new song").add(CommandFactory.argument("add", "required").example("add")).add(CommandFactory.argument("id", "The id to register the song as").example("chuchu")).add(CommandFactory.argument("url", "The url of the song to register").example("https://www.youtube.com/watch?v=5d32-RnUlAA")).requireRole(Roles.DJ).build();
		public static final CommandArgumentUsage REMOVE = CommandFactory.usage("remove", "Remove a registered song").add(CommandFactory.argument("remove", "required").example("remove")).add(CommandFactory.argument("id", "The id of the song to remove").example("chchu")).requireRole(Roles.DJ).build();
	}

	public static final Command HELP = CommandFactory.command("help", "Help with tavern commands").tag(Tags.GENERAL).add(HelpUsages.DEFAULT).add(HelpUsages.COMMAND_HELP).build();
	public static final Command ROLL = CommandFactory.command("roll", "Roll dice").tag(Tags.GENERAL).add(RollUsages.DEFAULT).add(RollUsages.SIDES).add(RollUsages.AMOUNT_AND_SIDES).add(RollUsages.AMOUNT_X_SIDES).usageRouter((usages, args) -> {
		switch (args.size()) {
            case 1:
				if (args.get(0).toLowerCase().contains("x")) {
					return RollUsages.AMOUNT_X_SIDES;
				} else if (args.get(0).toLowerCase().equals("tide")) {
					return RollUsages.ROLL_TIDE;
				} else {
					return RollUsages.SIDES;
				}
			case 2:
				return RollUsages.AMOUNT_AND_SIDES;
			case 0:
				// fall-through
			default:
				return RollUsages.DEFAULT;
		}
	}).build();
	public static final Command PLAY = CommandFactory.command("play", "Play a song").tag(Tags.MUSIC).add(CommandFactory.usage("song", "Play a song").varArgs().add(CommandFactory.argument("song", "the youtube url or song id").example("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))).build();
	public static final Command WEAVE = CommandFactory.command("weave", "Sets tavern to weave mode for a given song").tag(Tags.MUSIC).add(CommandFactory.usage("default", "Turns off Weave Mode")).add(CommandFactory.usage("song", "Song to be weaved into the queue").varArgs().add(CommandFactory.argument("song", "the youtube url or song id").example("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))).build();
	public static final Command PLAY_MODE = CommandFactory.command("pm", "Sets tavern to play mode for given category").tag(Tags.MUSIC).add(CommandFactory.usage("default", "Turns off modes")).add(CommandFactory.usage("category", "Category to be shuffled at the end of the queue").add(CommandFactory.argument("category", "the category string").example("elevator"))).build();
	public static final Command NOW_PLAYING = CommandFactory.command("np", "Get the current playing track").tag(Tags.MUSIC).add(CommandFactory.usage("default", "Get the current playing track")).build();
	public static final Command QUEUE = CommandFactory.command("queue", "Get the queue of music").tag(Tags.MUSIC).add(CommandFactory.usage("default", "Get the queue of music")).build();
	public static final Command STOP = CommandFactory.command("stop", "Stop and clear the music queue").tag(Tags.MUSIC).add(CommandFactory.usage("default", "Stop and clear the music queue")).build();
	public static final Command CLEAR = CommandFactory.command("clear", "Clear the music queue").tag(Tags.MUSIC).add(CommandFactory.usage("default", "Clear the music queue")).build();
	public static final Command SKIP = CommandFactory.command("skip", "Skip the current track").tag(Tags.MUSIC).add(CommandFactory.usage("default", "Skip the current track")).add(CommandFactory.usage("skip amount", "Skip an amount of songs").add(CommandFactory.argument("amount", "The amount of songs to skip").example("5"))).build();
	public static final Command SKIP_TIME = CommandFactory.command("st", "Skip seconds from playing track").tag(Tags.MUSIC).add(CommandFactory.usage("default", "Skip 60 seconds")).add(CommandFactory.usage("skip amount", "Skip X seconds").add(CommandFactory.argument("amount", "The amount of seconds to skip").example("60"))).build();
	public static final Command SHUFFLE = CommandFactory.command("shuffle", "Shuffle the current song queue").tag(Tags.MUSIC).add(CommandFactory.usage("default", "Shuffle the current song queue")).build();
	public static final Command PLAY_NEXT = CommandFactory.command("pn", "Play this song or playlist after the currently playing song").tag(Tags.MUSIC).add(CommandFactory.usage("song", "Play this song or playlist after the currently playing song").varArgs().add(CommandFactory.argument("song", "the youtube url or song id").example("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))).build();
	public static final Command PAUSE = CommandFactory.command("pause", "Pause the music").tag(Tags.MUSIC).add(CommandFactory.usage("default", "Pause the music")).build();
	public static final Command UNPAUSE = CommandFactory.command("unpause", "Resume the music").tag(Tags.MUSIC).add(CommandFactory.usage("default", "Resume the music")).build();
	public static final Command JOIN = CommandFactory.command("join", "Join the voice chat lobby").tag(Tags.MUSIC).add(CommandFactory.usage("default", "Join the voice chat lobby")).build();
	public static final Command LEAVE = CommandFactory.command("leave", "Leave the voice chat lobby").tag(Tags.MUSIC).add(CommandFactory.usage("default", "Leave the voice chat lobby")).build();
	public static final Command SONGS = CommandFactory.command("songs", "Commands for registered songs").tag(Tags.MUSIC).add(SongsUsages.DEFAULT).add(SongsUsages.ADD).add(SongsUsages.REMOVE).add(SongsUsages.ADD_WITH_CATEGORY).usageRouter((usages, args) -> {
		switch (args.size()) {
			case 0:
				return SongsUsages.DEFAULT;
			case 2:
				if (args.get(0).toLowerCase().contains("remove"))
					return SongsUsages.REMOVE;
				else return SongsUsages.DEFAULT;
			case 3:
				if (args.get(0).toLowerCase().contains("add"))
					return SongsUsages.ADD;
				else return SongsUsages.DEFAULT;
			case 4:
				if (args.get(0).toLowerCase().contains("add"))
					return SongsUsages.ADD_WITH_CATEGORY;
				else return SongsUsages.DEFAULT;
			default:
				return SongsUsages.DEFAULT;
		}
	}).build();
	public static final Command COMRADE = CommandFactory.command("comrade", "COMRADES!").tag(Tags.GENERAL).add(CommandFactory.usage("default", "Comrade mode")).build();
	public static final Command UNCOMRADE = CommandFactory.command("uncomrade", "...").tag(Tags.GENERAL).add(CommandFactory.usage("default", "Turn off comrade mode")).build();
	public static final Command DRINK = CommandFactory.command("drink", "Take some drinks").tag(Tags.DRINKING).add(CommandFactory.usage("drink", "Take 1-5 drinks")).build();
	public static final Command POPPOP = CommandFactory.command("poppop", "Pop pop!").tag(Tags.DRINKING).add(CommandFactory.usage("poppop", "Everyone takes 0-4 drinks")).build();
	public static final Command DRINKS = CommandFactory.command("drinks", "Drink totals").tag(Tags.DRINKING).add(CommandFactory.usage("drinks", "List total drinks for users in the voice channel")).build();

	public static List<Command> getCommands() {
		Field[] fields = TavernCommands.class.getDeclaredFields();
		return Arrays.stream(fields)
			.filter(field -> Command.class == field.getType() && field.trySetAccessible())
			.map(field -> {
				try {
					return (Command) field.get(null);
				} catch (Exception ex) {
					return null;
				}
			})
			.collect(Collectors.toList());
	}

}
