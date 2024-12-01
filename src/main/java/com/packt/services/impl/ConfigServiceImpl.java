package com.packt.services.impl;

import com.packt.repositories.ConfigJsonRepository;
import com.packt.repositories.impl.ConfigJsonRepositoryImpl;
import com.packt.services.ConfigService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigServiceImpl implements ConfigService {
    private final ConfigJsonRepository configJsonRepository = new ConfigJsonRepositoryImpl();

    @Override
    public void createGuildConfigFile(Path path) {
        configJsonRepository.createGuildConfigFile(path);
    }

    @Override
    public String getWelcomeMessage(Path path) {
        List<String> values = configJsonRepository.getJsonValue("welcome_message", path);
        if (values == null){
            return null;
        }
        return values.getFirst();
    }

    @Override
    public void setWelcomeMessage(Path path, String message) {
        configJsonRepository.setJsonValue("welcome_message", message, path);
    }

    @Override
    public List<Role> getRoles(Path path, Guild guild) {
        List<String> stringsRoles = configJsonRepository.getJsonValue("roles", path);
        List<Role> roles = new ArrayList<>();
        for (String stringRole : stringsRoles) {
            Role role = guild.getRolesByName(stringRole, true).getFirst();
            if(role == null){
                System.out.println("There is no role: " + stringRole + " on this server!");
                continue;
            }
            roles.add(role);
        }
        return roles;
    }

    @Override
    public boolean addRole(Path path, Role role, Guild guild) {
        List<Role> roles = getRoles(path, guild);
        if(roles.contains(role)){
            return false;
        }
        roles.add(role);
        configJsonRepository.setJsonValue("roles", roles.stream().map(Role::getName).toList(), path);
        return true;
    }

    @Override
    public boolean removeRole(Path path, Role role, Guild guild) {
        List<Role> roles = getRoles(path, guild);
        boolean flag = roles.remove(role);
        configJsonRepository.setJsonValue("roles", roles.stream().map(Role::getName).toList(), path);
        return flag;
    }

    @Override
    public void setCategory(Path path, Category category, Guild guild) {
        configJsonRepository.setJsonValue("games_category_id", category.getId(), path);
    }

    @Override
    public void setChannel(Path path, String channelJsonKey, TextChannel channel, Guild guild) {
        configJsonRepository.setJsonValue(channelJsonKey, channel.getId(), path);
    }

    @Override
    public Category getGamesCategory(Path path, Guild guild) {
        List<String> values = configJsonRepository.getJsonValue("games_category_id", path);
        if (values == null){
            return null;
        }
        String categoryId = values.getFirst();
        return guild.getCategoryById(categoryId);
    }

    @Override
    public TextChannel getChannel(Path path, String channelJsonKey, Guild guild) {
        List<String> values = configJsonRepository.getJsonValue(channelJsonKey, path);
        if (values == null){
            return null;
        }
        String channelId = values.getFirst();
        return guild.getTextChannelById(channelId);
    }
}
