package com.tavern.discord.audio;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.discord.GuildId;
import com.tavern.utilities.StringUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class SkipTimeCommandHandler implements CommandHandler {
    private final AudioService audioService;

    public SkipTimeCommandHandler(AudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public Command getCommand() {
        return TavernCommands.SKIP_TIME;
    }

    @Override
    public boolean supportsUsage(CommandArgumentUsage usage) {
        return true;
    }

    @Override
    public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
        final int skipTime = StringUtils.isNullOrBlank(message.getArgs().get(0)) ? 60 : Integer.parseInt(message.getArgs().get(0));
        GuildId guildId = new GuildId(event.getGuild().getId());
        final String title = audioService.getNowPlaying(guildId).getInfo().getTitle();
        event.getChannel().sendMessage("Skipping " + skipTime + " seconds from " + title).queue();

        audioService.skipTime(guildId, skipTime);
        return new CommandResultBuilder().success().build();
    }

}
