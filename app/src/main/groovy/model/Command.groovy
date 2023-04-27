package model

class Command {

    private final String id
    private final String guildId
    private final String commandString
    private final String args
    private final String userId
    private final String textChannel

    // Data needed:
    // TextChannel to output message / Optionally could just get return string.
    // GuildId to get tavern instance, could just get this from user.voicestate.... but you should be able to issue commands without being in a voice channel.
    // UserId for finding their voice channel

    protected Command(){

    }

    String getId() {
        return id
    }

    String getGuildId() {
        return guildId
    }

    String getCommandString() {
        return commandString
    }

    String getArgs() {
        return args
    }

    String getUserId() {
        return userId
    }

    String getTextChannel() {
        return textChannel
    }

    String getCommandMessage() {
        return commandString + " " + args
    }
}
