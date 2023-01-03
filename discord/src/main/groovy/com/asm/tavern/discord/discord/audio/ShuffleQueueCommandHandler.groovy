package com.asm.tavern.discord.discord.audio

import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.command.Command
import com.asm.tavern.domain.model.command.CommandArgumentUsage
import com.asm.tavern.domain.model.command.CommandHandler
import com.asm.tavern.domain.model.command.CommandMessage
import com.asm.tavern.domain.model.command.CommandResult
import com.asm.tavern.domain.model.discord.GuildId
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull

class ShuffleQueueCommandHandler implements CommandHandler {
    private final AudioService audioService

    ShuffleQueueCommandHandler(AudioService audioService) {
        this.audioService = audioService
    }

    @Override
    Command getCommand() {
        TavernCommands.SHUFFLE
    }

    @Override
    boolean supportsUsage(CommandArgumentUsage usage) {
        true
    }

    @Override
    CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
        audioService.shuffle(new GuildId(event.guild.id))
        event.getChannel().sendMessage("Shuffled the queue!").queue()
    }
}
