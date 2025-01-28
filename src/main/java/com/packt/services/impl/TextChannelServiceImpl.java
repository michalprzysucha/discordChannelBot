package com.packt.services.impl;

import com.packt.services.ConfigService;
import com.packt.services.TextChannelService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class TextChannelServiceImpl implements TextChannelService {
    private final ConfigService configService = new ConfigServiceImpl();

    @Override
    public ChannelAction<TextChannel> createChannel(Guild guild, Member... members) {
        Member playerA = members[0];
        Member playerB = members[1];
        if(playerA == null || playerB == null) {
            return null;
        }
        Category category = configService.getCategory(Path.of(guild.getName() + ".json"), "games_category_id", guild);
        if (category == null) {
            return  null;
        }
        String playerAName = playerA.getEffectiveName().toLowerCase();
        String playerBName = playerB.getEffectiveName().toLowerCase();
        String channelName = "%s-%s".formatted(playerAName, playerBName);
        if (matchChannelExists(playerAName, playerBName, Objects.requireNonNull(category))) {
            return null;
        }
        return guild.createTextChannel(channelName, category);
    }

    @Override
    public boolean channelExists(String channelName, Category category) {
        for (TextChannel textChannel : category.getTextChannels()) {
            if (textChannel.getName().equals(channelName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matchChannelExists(String playerAName, String playerBName, Category category) {
        return channelExists("%s-%s".formatted(playerAName, playerBName), category) ||
                channelExists("%s-%s".formatted(playerBName, playerAName), category);
    }

    @Override
    public ChannelAction<TextChannel> editChannel(ChannelAction<TextChannel> action, Guild guild, Member... members) {
        Member guildOwner = guild.getOwner();
        Member playerA = members[0];
        Member playerB = members[1];
        action = hideChannel(action, guild);

        List<Role> roleList = configService.getRoles(Path.of(guild.getName() + ".json"), guild);
        if (roleList != null && !roleList.isEmpty()) {
            for (Role role : roleList) {
                action = addRoleToChannel(action, role);
            }
        }
        action = addMemberToChannel(action, guildOwner);
        action = addMemberToChannel(action, playerA);
        action = addMemberToChannel(action, playerB);
        action = addMemberToChannel(action, guild.getSelfMember());
        return action;
    }

    @Override
    public ChannelAction<TextChannel> hideChannel(ChannelAction<TextChannel> action, Guild guild) {
        return action.addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL));
    }

    @Override
    public ChannelAction<TextChannel> addMemberToChannel(ChannelAction<TextChannel> action, Member member) {
        return action.addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null);
    }

    @Override
    public ChannelAction<TextChannel> addRoleToChannel(ChannelAction<TextChannel> action, Role role) {
        return action.addPermissionOverride(role, EnumSet.of(Permission.VIEW_CHANNEL), null);
    }
}
