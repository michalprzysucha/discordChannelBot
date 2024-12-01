package com.packt.services;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

public interface TextChannelService {
    boolean channelExists(String channelName, Category category);
    boolean matchChannelExists(String playerAName, String playerBName, Category category);
    ChannelAction<TextChannel> createChannel(Guild guild, Member... members);
    ChannelAction<TextChannel> editChannel(ChannelAction<TextChannel> action, Guild guild, Member... members);
    ChannelAction<TextChannel> hideChannel(ChannelAction<TextChannel> action, Guild guild);
    ChannelAction<TextChannel> addMemberToChannel(ChannelAction<TextChannel> action, Member member);
    ChannelAction<TextChannel> addRoleToChannel(ChannelAction<TextChannel> action, Role role);
}
