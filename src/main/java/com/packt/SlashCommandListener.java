package com.packt;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class SlashCommandListener extends ListenerAdapter {
    private static final String targetCategoryName = "mecze";

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "say" -> event.reply(Objects.requireNonNull(event.getOption("content")).getAsString()).queue();
            case "create-channel" -> createChannelHandler(event);
            default -> event.reply("Unknown command!").setEphemeral(true).queue();
        }
    }

    private void createChannelHandler(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        Member playerA = Objects.requireNonNull(event.getOption("first-player")).getAsMember();
        Member playerB = Objects.requireNonNull(event.getOption("second-player")).getAsMember();
        var matchChannel = createMatchChannel(guild, playerA, playerB);
        if(matchChannel == null) {
            event.reply("Match channel already exists!").setEphemeral(true).queue();
            return;
        }
        editChannel(matchChannel, guild, playerA, playerB).queue(
                        (TextChannel channel) -> event.reply("Text channel `" + channel.getName() + "` has been created!").queue(),
                        (Throwable error) -> event.reply("Failed to create the channel.").setEphemeral(true).queue()
        );
    }

    private ChannelAction<TextChannel> createMatchChannel(Guild guild, Member playerA, Member playerB) {
        Category category = guild.getCategoriesByName(targetCategoryName, true).getFirst();
        String playerAName = playerA.getEffectiveName().toLowerCase();
        String playerBName = playerB.getEffectiveName().toLowerCase();
        if (matchChannelExists(playerAName, playerBName, category)) {
            return null;
        }
        return guild.createTextChannel("%s-%s".formatted(playerAName, playerBName), category);
    }

    private ChannelAction<TextChannel> editChannel(ChannelAction<TextChannel> action, Guild guild, Member playerA, Member playerB) {
        Member guildOwner = guild.getOwner();
        Role adminRole = guild.getRolesByName("Admin", true).getFirst();
        Role streamerRole = guild.getRolesByName("Streaming", true).getFirst();
        Role modRole = guild.getRolesByName("Mod", true).getFirst();
        List<Role> roleList = List.of(adminRole, streamerRole, modRole);

        action = hideChannel(action, guild);
        for(Role role : roleList) {
            action = addRoleToChannel(action, role);
        }
        action = addMemberToChannel(action, guildOwner);
        action = addMemberToChannel(action, playerA);
        action = addMemberToChannel(action, playerB);
        return action;
    }

    private ChannelAction<TextChannel> hideChannel(ChannelAction<TextChannel> action, Guild guild){
        return action.addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL));
    }

    private ChannelAction<TextChannel> addMemberToChannel(ChannelAction<TextChannel> action, Member member) {
        return action.addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null);
    }

    private ChannelAction<TextChannel> addRoleToChannel(ChannelAction<TextChannel> action, Role role) {
        return action.addPermissionOverride(role, EnumSet.of(Permission.VIEW_CHANNEL), null);
    }

    private boolean matchChannelExists(String playerAName, String playerBName, Category category) {
        return channelExists("%s-%s".formatted(playerAName, playerBName), category) ||
                channelExists("%s-%s".formatted(playerBName, playerAName), category);
    }

    private boolean channelExists(String channelName, Category category) {
        for (TextChannel textChannel : category.getTextChannels()) {
            if (textChannel.getName().equals(channelName)) {
                return true;
            }
        }
        return false;
    }
}