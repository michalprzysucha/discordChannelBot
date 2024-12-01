package com.packt.services;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.nio.file.Path;
import java.util.List;

public interface ConfigService {
    void createGuildConfigFile(Path path);
    void setWelcomeMessage(Path path, String message);
    void setCategory(Path path, Category category, Guild guild);
    void setChannel(Path path, String channelJsonKey, TextChannel channel, Guild guild);
    boolean removeRole(Path path, Role role, Guild guild);
    boolean addRole(Path path, Role role, Guild guild);
    Category getGamesCategory(Path path, Guild guild);
    TextChannel getChannel(Path path, String channelJsonKey, Guild guild);
    String getWelcomeMessage(Path path);
    List<Role> getRoles(Path path, Guild guild);
}
