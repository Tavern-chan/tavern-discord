package controller

import com.asm.tavern.discord.app.App
import com.asm.tavern.discord.discord.DiscordListener
import com.asm.tavern.domain.model.discord.UserId
import model.Command
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.GenericEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CommandController {

    // Data needed:
    // TextChannel to output message
    // Guildid -> Guild object
    // Guild to get tavern instance
    // UserId -> Member Object
    // UserId for finding their voice channel

    @PostMapping("/api/command")
    String commandHandler(@RequestBody final Command command){
        JDA jda = App.TavernApiServer.getJda()
        DiscordListener discordListener = jda.eventManager.registeredListeners.first() as DiscordListener

        Guild guild = jda.getGuildById(command.guildId)

        UserId userId = new UserId(command.userId)
        Member member = guild.getMemberById(command.userId)

        TextChannel textChannel = guild.getTextChannelById(command.textChannel)



        //TextChannel textChannel = null, Guild guild, Member member, String message
        discordListener.onApiCallReceived(textChannel, guild, member, command.getCommandMessage())




        String responseString = ""





        return responseString
    }

    @GetMapping("/nowPlaying")
    String nowPlaying(){
        String returnString = ""
        JDA jda = App.TavernApiServer.getJda()
        DiscordListener discordListener = jda.eventManager.registeredListeners.first() as DiscordListener
        for(Guild g : jda.getGuilds()){
            returnString += g.name + g.description
        }
        return (returnString)

    }

    AudioChannelUnion getAudioChannelUnionOfUser(UserId userId, JDA jda){
        // Given a user id, return the text channel that the bot resides in? Maybe just private message the user for responses?
        Guild guild = jda.getGuilds().stream()
                .filter(
                        guild -> guild.getMemberById(userId.id)
                ).findFirst() as Guild
        return guild.getMemberById(userId.id).getVoiceState().getChannel()
    }

    Channel getTextChannelById(UserId userId, JDA jda){
        // Given a user id, return the text channel that the bot resides in? Maybe just private message the user for responses?
        /*Optional<Guild> guild = Optional.of(jda.getGuilds().stream()
                .filter(
                        guild -> guild.getTextChannelsByName("tavern", true) != null && guild.getMemberById(userId.id)
                ).findFirst()*/

        return jda.getTextChannels().first()

    }
}
