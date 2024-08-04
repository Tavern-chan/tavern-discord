package com.tavern.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.tavern.domain.model.audio.ActiveAudioTrack;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.audio.AudioTrackInfo;
import com.tavern.domain.model.discord.GuildId;
import com.tavern.domain.model.discord.VoiceChannelId;
import com.tavern.utilities.CollectionUtils;
import com.tavern.utilities.Ref;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class LavaPlayerAudioService implements AudioService {
	private static final XLogger logger = XLoggerFactory.getXLogger(LavaPlayerAudioService.class);
	private static final Logger log = LoggerFactory.getLogger(LavaPlayerAudioService.class);

	private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
	private final dev.lavalink.youtube.YoutubeAudioSourceManager ytSourceManager = new dev.lavalink.youtube.YoutubeAudioSourceManager();
	private final Map<GuildId, GuildMusicManager> musicManagers = new HashMap<GuildId, GuildMusicManager>();
	private final String YOUTUBE_SEARCH_PREFIX = "ytsearch:";
	private final ModeService modeService;

	public LavaPlayerAudioService(ModeService modeService) {
		playerManager.registerSourceManager(ytSourceManager);
		// TODO: EMM Is this still needed?
		AudioSourceManagers.registerRemoteSources(playerManager, YoutubeAudioSourceManager.class);
		AudioSourceManagers.registerLocalSource(playerManager);
		this.modeService = modeService;
	}

	@Override
	public void join(GuildVoiceState voiceState, AudioManager audioManager) {
		if (null != voiceState && null != voiceState.getChannel()) {
			audioManager.openAudioConnection(voiceState.getChannel());
			getGuildAudioPlayer(voiceState).setVoiceChannelId(new VoiceChannelId(voiceState.getChannel().getId()));
			getGuildAudioPlayer(voiceState).getScheduler().setModeService(this.modeService);
		}
	}

	@Override
	public void leave(Guild guild) {
		guild.getAudioManager().closeAudioConnection();
		musicManagers.get(new GuildId(guild.getId())).setVoiceChannelId(null);
	}

	@Override
	public VoiceChannelId getCurrentChannel(GuildId guildId) {
		return musicManagers.get(guildId).getVoiceChannelId();
	}

	@Override
	public void play(final TextChannel textChannel, final URI uri) {
		final GuildId guildId = new GuildId(textChannel.getGuild().getId());
		final GuildMusicManager musicManager = musicManagers.get(guildId);
		musicManager.getScheduler().setChannelId(textChannel);

		playerManager.loadItemOrdered(musicManager, uri.toString(), new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(final AudioTrack track) {
				if (null != getNowPlaying(guildId)) {
					textChannel.sendMessage("Adding to queue " + track.getInfo().title).queue();
				}

				scheduleTrack(musicManager, track);
			}

			@Override
			public void playlistLoaded(final AudioPlaylist playlist) {
				AudioTrack firstTrack = Optional.ofNullable(playlist.getSelectedTrack())
					.orElseGet(() -> CollectionUtils.first(playlist.getTracks()).orElse(null));

				if (firstTrack == null) {
					logger.error("Schedule playlist but there were no tracks");
					return;
				}

				int currentIndex = playlist.getTracks().indexOf(firstTrack);
				if (currentIndex == -1) {
					logger.warn("Could not find track index, starting from the top");
					currentIndex = 0;
				}

				for (int i = currentIndex; i < playlist.getTracks().size(); i++) {
					scheduleTrack(musicManager, playlist.getTracks().get(i));
				}

				textChannel.sendMessage("Adding playlist " + playlist.getName() + " to queue. Starting with the track " + playlist.getTracks().get(currentIndex).getInfo().title).queue();
			}

			@Override
			public void noMatches() {
				textChannel.sendMessage("Nothing found by " + uri);
			}

			@Override
			public void loadFailed(final FriendlyException exception) {
				textChannel.sendMessage("Could not play: " + exception.getMessage()).queue();
			}

		});
	}

	@Override
	public void play(final TextChannel textChannel, final String searchString) {
		final GuildId guildId = new GuildId(textChannel.getGuild().getId());
		final GuildMusicManager musicManager = musicManagers.get(guildId);
		musicManager.getScheduler().setChannelId(textChannel);


		playerManager.loadItemOrdered(musicManager, YOUTUBE_SEARCH_PREFIX + searchString, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(final AudioTrack track) {
				if (null != getNowPlaying(guildId)) {
					textChannel.sendMessage("Adding to queue " + track.getInfo().title).queue();
				}

				scheduleTrack(musicManager, track);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = Optional.ofNullable(playlist.getSelectedTrack())
					.orElseGet(() -> CollectionUtils.first(playlist.getTracks()).orElse(null));

				if (firstTrack == null) {
					logger.error("Added playlist but there were no tracks");
					return;
				}

				if (null != getNowPlaying(guildId)) {
					textChannel.sendMessage("Adding to queue " + firstTrack.getInfo().title).queue();
				}

				scheduleTrack(musicManager, firstTrack);
			}

			@Override
			public void noMatches() {
				textChannel.sendMessage("Nothing found by " + searchString);
			}

			@Override
			public void loadFailed(final FriendlyException exception) {
				textChannel.sendMessage("Could not play: " + exception.getMessage()).queue();
			}

		});
	}

	@Override
	public void playNext(final TextChannel textChannel, final URI uri) {
		final GuildId guildId = new GuildId(textChannel.getGuild().getId());
		final GuildMusicManager musicManager = musicManagers.get(guildId);
		musicManager.getScheduler().setChannelId(textChannel);


		playerManager.loadItemOrdered(musicManager, uri.toString(), new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(final AudioTrack track) {
				if (null == getNowPlaying(guildId)) {
					musicManagers.get(guildId).getScheduler().playNext(track);
				} else {
					textChannel.sendMessage("Adding " + track.getInfo().title + " to queue after currently playing song.").queue();
					musicManagers.get(guildId).getScheduler().playNext(track);
				}
			}

			@Override
			public void playlistLoaded(final AudioPlaylist playlist) {
				AudioTrack firstTrack = Optional.ofNullable(playlist.getSelectedTrack())
					.orElseGet(() -> CollectionUtils.first(playlist.getTracks()).orElse(null));

				if (firstTrack == null) {
					logger.warn("Added playlist but there were no tracks");
				}

				int currentIndex = playlist.getTracks().indexOf(firstTrack);
				if (currentIndex == -1) {
					logger.warn("Could not find track index, starting from the top");
					currentIndex = 0;
				}

				// if nothing in queue, we just add the playlist normally else we add it in reverse order to the position this maintains playlist order while placing in queue at correct position

				if (null == getNowPlaying(guildId)) {
					for (int i = currentIndex; i < playlist.getTracks().size(); i++) {
						scheduleTrack(musicManager, playlist.getTracks().get(i));
					}

					textChannel.sendMessage("Adding playlist " + playlist.getName() + " to queue. Starting with the track " + playlist.getTracks().get(currentIndex).getInfo().title).queue();
				} else {
					// Modified from play command, if adding a playlist to play next we would want to reverse order the list when sending it to queue
					// This is because adding a playlist one song at a time to a specific position would reverse order.
					// a,b,c + playlist(1,2,3) by going a,b,c + 1; a,b,c,1 + 2; etc... =a,b,c,1,2, 3 This works for just adding to the end of the queue
					// a,b,c + playlist(1,2,3) by going 1,a,b,c; 2,1,a,b,c; etc... = 3,2,1,a,b,c But when placing at the first position would mess up the order of the list added
					for (int i = playlist.getTracks().size(); i > 0; i--) {
						musicManagers.get(guildId).getScheduler().playNext(playlist.getTracks().get(i - 1));
					}

					textChannel.sendMessage("Adding playlist " + playlist.getName() + " to queue after currently playing song. Starting with the track " + playlist.getTracks().get(currentIndex).getInfo().title).queue();
				}
			}

			@Override
			public void noMatches() {
				textChannel.sendMessage("Nothing found by " + uri);
			}

			@Override
			public void loadFailed(final FriendlyException exception) {
				textChannel.sendMessage("Could not play: " + exception.getMessage()).queue();
			}
		});
	}

	@Override
	public void playNext(final TextChannel textChannel, final String searchString) {
		final GuildId guildId = new GuildId(textChannel.getGuild().getId());
		final GuildMusicManager musicManager = musicManagers.get(guildId);
		musicManager.getScheduler().setChannelId(textChannel);

		// playNext with a search string gets a playlist back from the search, so handle it like we only want the first song.
		playerManager.loadItemOrdered(musicManager, YOUTUBE_SEARCH_PREFIX + searchString, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(final AudioTrack track) {
				if (null == getNowPlaying(guildId)) {
					scheduleTrack(musicManager, track);
				} else {
					textChannel.sendMessage("Adding " + track.getInfo().title + " to queue after currently playing song.").queue();
					scheduleTrackNext(musicManager, track);
				}
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				final AudioTrack firstTrack = Optional.ofNullable(playlist.getSelectedTrack())
					.orElseGet(() -> CollectionUtils.first(playlist.getTracks()).orElse(null));

				if (firstTrack == null) {
					logger.error("Added playlist but there were no tracks");
					return;
				}

				if (null == getNowPlaying(guildId)) {
					scheduleTrack(musicManager, CollectionUtils.first(playlist.getTracks()).orElse(null));
				} else {
					scheduleTrackNext(musicManager, CollectionUtils.first(playlist.getTracks()).orElse(null));
					textChannel.sendMessage("Adding " + firstTrack.getInfo().title + " to queue after currently playing song.").queue();
				}
			}

			@Override
			public void noMatches() {
				textChannel.sendMessage("Nothing found by " + searchString).queue();
			}

			@Override
			public void loadFailed(final FriendlyException exception) {
				textChannel.sendMessage("Could not play: " + exception.getMessage()).queue();
				logger.error("Failed to play track", exception);
			}
		});
	}

	private void scheduleTrack(GuildMusicManager musicManager, AudioTrack track) {
		musicManager.getScheduler().queue(track);
	}

	private void scheduleTrackNext(GuildMusicManager musicManager, AudioTrack track) {
		musicManager.getScheduler().playNext(track);
	}

	private synchronized GuildMusicManager getGuildAudioPlayer(GuildVoiceState guildVoiceState) {
		GuildId guildId = new GuildId(guildVoiceState.getGuild().getId());
		GuildMusicManager musicManager = musicManagers.get(guildId);

		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager, new VoiceChannelId(guildVoiceState.getChannel().getId()));
			musicManagers.put(guildId, musicManager);
		}

		guildVoiceState.getGuild().getAudioManager().setSendingHandler(musicManager.getSendHandler());

		return musicManager;
	}

	@Override
	public void skip(GuildId guildId, int amount) {
		GuildMusicManager manager = musicManagers.get(guildId);
		if (null != manager) {
			manager.getScheduler().skip(amount);
		}

	}

	@Override
	public void skipTime(GuildId guildId, int amount) {
		GuildMusicManager manager = musicManagers.get(guildId);
		if (null != manager) {
			manager.getScheduler().skipTime(amount);
		}

	}

	@Override
	public void stop(GuildId guildId) {
		GuildMusicManager manager = musicManagers.get(guildId);
		if (null != manager) {
			manager.getScheduler().stopAndClear();
		}

	}

	@Override
	public void clear(GuildId guildId) {
		GuildMusicManager manager = musicManagers.get(guildId);
		if (null != manager) {
			manager.getScheduler().clear();
		}

	}

	@Override
	public void pause(GuildId guildId) {
		GuildMusicManager manager = musicManagers.get(guildId);
		if (null != manager) {
			manager.getScheduler().pause();
		}

	}

	@Override
	public void unpause(GuildId guildId) {
		GuildMusicManager manager = musicManagers.get(guildId);
		if (null != manager) {
			manager.getScheduler().unpause();
		}

	}

	@Override
	public List<AudioTrackInfo> getQueue(GuildId guildId) {
		return musicManagers.get(guildId).getScheduler().getQueue().stream().map(track ->
			new AudioTrackInfoAdapter(track.getInfo())
		).collect(Collectors.toList());
	}

	@Override
	public ActiveAudioTrack getNowPlaying(GuildId guildId) {
		return Optional.ofNullable(musicManagers.get(guildId))
			.map(GuildMusicManager::getScheduler)
			.map(TrackScheduler::getNowPlaying)
			.map(ActiveAudioTrackAdapter::new)
			.orElse(null);
	}

	@Override
	public void shuffle(GuildId guildId) {
		musicManagers.get(guildId).getScheduler().shuffle();
	}

	@Override
	public boolean getIsPaused(GuildId guildId) {
		return musicManagers.get(guildId).getScheduler().getIsPaused();
	}

	@Override
	public AudioTrack getAudioTrack(String searchString) {
		final Ref<AudioTrack> trackRef = new Ref<>();

		try {
			playerManager.loadItem(YOUTUBE_SEARCH_PREFIX + searchString, new AudioLoadResultHandler() {
				@Override
				public void trackLoaded(AudioTrack track) {
					trackRef.set(track);
				}

				@Override
				public void playlistLoaded(AudioPlaylist playlist) {
					trackRef.set(playlist.getSelectedTrack());

					if (trackRef.get() == null) {
						playlist.getTracks().stream().findFirst()
							.ifPresent(trackRef::set);
					}
				}

				@Override
				public void noMatches() { }

				@Override
				public void loadFailed(FriendlyException exception) { }

			}).get();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to load audio track", ex);
		}

		return trackRef.get();
	}

	@Override
	public AudioTrack getAudioTrack(URI uri) {
		final Ref<AudioTrack> trackRef = new Ref<>();

		try {
			playerManager.loadItem(uri.toString(), new AudioLoadResultHandler() {
				@Override
				public void trackLoaded(AudioTrack track) {
					trackRef.set(track);
				}

				@Override
				public void playlistLoaded(AudioPlaylist playlist) {
					trackRef.set(playlist.getSelectedTrack());

					if (trackRef.get() == null) {
						trackRef.set(CollectionUtils.first(playlist.getTracks()).orElse(null));
					}
				}

				@Override
				public void noMatches() { }

				@Override
				public void loadFailed(FriendlyException exception) { }

			}).get();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to load audio track", ex);
		}

		return trackRef.get();
	}

	@Override
	public void setWeaveAudio(URI uri) {
		modeService.setWeave(getAudioTrack(uri));
	}

	@Override
	public void setCategory(String category) {
		modeService.setCategory(category, this);
	}

	@Override
	public void setWeaveAudio(String searchString) {
		modeService.setWeave(getAudioTrack(searchString));
	}

	@Override
	public void clearPlayMode() {
		modeService.defaultMode();
	}

	@Override
	public void forcePlay(TextChannel textChannel) {
		GuildId guildId = new GuildId(textChannel.getGuild().getId());
		GuildMusicManager musicManager = musicManagers.get(guildId);
		musicManager.getScheduler().setChannelId(textChannel);
		musicManagers.get(guildId).getScheduler().forceNext();
	}
}
