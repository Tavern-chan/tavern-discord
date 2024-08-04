package com.tavern.discord.audio;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.discord.GuildId;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class ShuffleQueueCommandHandler implements CommandHandler {
    private final AudioService audioService;

    public ShuffleQueueCommandHandler(AudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public Command getCommand() {
        return TavernCommands.SHUFFLE;
    }

    @Override
    public boolean supportsUsage(CommandArgumentUsage usage) {
        return true;
    }

    @Override
    public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
        audioService.shuffle(new GuildId(event.getGuild().getId()));
        event.getChannel().sendMessage("Shuffled the queue!").queue();
        return new CommandResultBuilder().success().build();
    }

}
