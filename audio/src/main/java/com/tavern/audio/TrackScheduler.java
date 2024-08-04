package com.tavern.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import kotlin.ranges.IntRange;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.IntStream;

public class TrackScheduler extends AudioEventAdapter {
	private static final XLogger logger = XLoggerFactory.getXLogger(TrackScheduler.class);

	private final AudioPlayer player;
	private BlockingQueue<AudioTrack> queue;
	private TextChannel textChannel;
	private final int maxRetryCount = 3;
	private int retryCount = 0;
	private ModeService modeService;

	/**
	 * @param player The audio player this scheduler uses
	 */
	public TrackScheduler(AudioPlayer player) {
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
	}

	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 *
	 * @param track The track to play or add to queue.
	 */
	public void queue(AudioTrack track) {
		// Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the player was already playing so this
		// track goes to the queue instead.
		if (!player.startTrack(track, true)) {
			if (!queue.offer(track)) {
				logger.error("Failed to add track {} to the queue", track.getInfo().title);
			}
		}
	}

	public void playNext(AudioTrack track) {
		if (!queue.isEmpty() || ModeService.MODE.CATEGORY.equals(modeService.getMode())) {
			// add new song to queue position 0 ie: next
            List<AudioTrack> songList = new ArrayList<>(queue);
			songList.add(0, track);

            this.queue = new LinkedBlockingQueue<>(songList);
		} else {
			queue(track);
		}
	}

	public void forceNext() {
		if (modeService.getMode() != null && !modeService.getMode().equals(ModeService.MODE.DEFAULT)) {
			switch (modeService.getMode()) {
				case CATEGORY:
					if (queue.isEmpty()) {
						AudioTrack modeTrack = modeService.getNext();
						playNext(modeTrack.makeClone());
					}
					break;
				case WEAVE:
					AudioTrack weaveTrack = modeService.getNext();
					if (!Objects.equals(weaveTrack.getInfo().title, getNowPlaying().getInfo().title)) {
						playNext(weaveTrack.makeClone());
					}
					break;
			}
		}

		nextTrack();
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	public void nextTrack() {
		// Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the player.
		player.startTrack(queue.poll(), false);
	}

	public void skip(int amount) {
		IntStream.range(1, amount).forEach(i -> queue.poll());
		player.stopTrack();
	}

	public void skipTime(int amount) {
		if (player.getPlayingTrack().isSeekable()) {
			player.getPlayingTrack().setPosition(player.getPlayingTrack().getPosition() + (amount * 1000));
		}
	}

	public void stopAndClear() {
		clear();
		player.stopTrack();
	}

	public void clear() {
		queue.clear();
	}

	public void pause() {
		player.setPaused(true);
	}

	public void unpause() {
		player.setPaused(false);
	}

	public boolean getIsPaused() {
		return player.isPaused();
	}

	public void shuffle() {
		// Shuffle and reconstruct the Queue
        List<AudioTrack> songList = new ArrayList<>(queue);
		Collections.shuffle(songList);
        this.queue = new LinkedBlockingQueue<>(songList);
	}

	public void setChannelId(TextChannel textChannel) {
		this.textChannel = textChannel;
	}

	public void setModeService(ModeService modeService) {
		this.modeService = modeService;
	}

	public BlockingQueue<AudioTrack> getQueue() {
		return queue;
	}

	public AudioTrack getNowPlaying() {
		return player.getPlayingTrack();
	}

	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		player.startTrack(queue.poll(), true);
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		// Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
		if (AudioTrackEndReason.LOAD_FAILED.equals(endReason)) {
			if (retryCount < 3) {
				retryCount++;
				textChannel.sendMessage("Failure playing track: " + track.getInfo().title + " attempting to retry. Attempt Number: " + retryCount).queue();
				player.startTrack(track.makeClone(), false);
			} else {
				textChannel.sendMessage("Failed to play track: " + track.getInfo().title + " " + retryCount + " times, track will be skipped. Video may be unavailable." + retryCount).queue();
				retryCount = 0;
			}

		} else if (endReason.mayStartNext || AudioTrackEndReason.STOPPED.equals(endReason)) {
			if (modeService.getMode() != null && ModeService.MODE.DEFAULT.equals(modeService.getMode())) {
				switch (modeService.getMode()) {
					case CATEGORY:
						if (queue.isEmpty()) {
							AudioTrack modeTrack = modeService.getNext();
							playNext(modeTrack.makeClone());
						}
						break;
					case WEAVE:
						AudioTrack weaveTrack = modeService.getNext();
						if (!weaveTrack.getInfo().title.equals(track.getInfo().title)) {
							playNext(weaveTrack.makeClone());
						}
						break;
				}
			}
			nextTrack();
		}
	}

	@Override
	public void onTrackStart(final AudioPlayer player, final AudioTrack track) {
		final Function<Duration, String> formatTime = (Duration duration) -> String.format(
			"%d:%2d",
			Long.valueOf(duration.getSeconds() / 60).intValue(),
			Long.valueOf(duration.getSeconds() % 60).intValue()
		).replace(" ", "0");

		Function<String, String> getVideoImageID = (String videoUrl) -> {
			try {
				String videoImageId = videoUrl.split("(?<=watch\\?v=)")[1];
				return String.format("https://img.youtube.com/vi/%s/sddefault.jpg", videoImageId);
			} catch (Exception ex) {
				logger.info("No VideoImage Found", ex);
				return null;
			}
		};

		EmbedBuilder eb = new EmbedBuilder();
		try {
			String videoImgUrl = getVideoImageID.apply(track.getInfo().uri);
			eb.setTitle(track.getInfo().title, track.getInfo().uri);// large hyperlink
			eb.setThumbnail(videoImgUrl);// Top right corner image
		} catch (Exception ex) {
			eb.setTitle(track.getInfo().title);
			logger.info("Video Image was unable to be fetched", ex);
		}

        eb.setDescription("By: " + track.getInfo().author);
		eb.addField(
			"Duration:",
			String.format(
				"%s/%s",
				formatTime.apply(Duration.ofMillis(player.getPlayingTrack().getPosition())),
				formatTime.apply(Duration.ofMillis(player.getPlayingTrack().getInfo().length))
			),
			false
		);
		eb.setColor(0x5865F2);// blurple UwU

		textChannel.sendMessageEmbeds(eb.build())
			.setActionRow(Button.primary("skip", "Skip"), Button.primary("shuffle", "Shuffle"), Button.primary("pause", "Play/Pause"))
			.queue();
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		logger.error("Exception when playing track {}", track.getInfo().title);
		logger.catching(exception);
	}
}
