/*
 * Copyright 2018 Alex Thomson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lxgaming.discordmusic.managers;

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.config.Group;
import io.github.lxgaming.discordmusic.configuration.config.Server;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.Set;

public class GroupManager {
    
    public static void registerServer(Guild guild) {
        Set<Group> groups = Toolbox.newLinkedHashSet();
        for (Role role : guild.getRoles()) {
            Group group = getGroup(role).orElse(new Group());
            group.setId(role.getIdLong());
            group.setName(role.getName());
            groups.add(group);
        }
        
        Server server = getServer(guild).orElse(new Server());
        server.setId(guild.getIdLong());
        server.setName(guild.getName());
        server.setGroups(groups);
        
        DiscordMusic.getInstance().getConfig().map(Config::getServers).ifPresent(servers -> servers.add(server));
        DiscordMusic.getInstance().getConfiguration().saveConfiguration();
    }
    
    public static boolean hasPermission(Member member, String permission) {
        Set<String> permissions = getPermissions(member);
        if (permissions.isEmpty() || permissions.contains("!" + permission)) {
            return false;
        }
        
        return Toolbox.containsIgnoreCase(permissions, permission) || Toolbox.containsIgnoreCase(permissions, "*");
    }
    
    public static Set<String> getPermissions(Member member) {
        Set<String> permissions = Toolbox.newLinkedHashSet();
        for (Group group : getGroups(member)) {
            permissions.addAll(getPermissions(group));
        }
        
        return permissions;
    }
    
    public static Set<String> getPermissions(Group group) {
        Set<String> permissions = Toolbox.newLinkedHashSet();
        for (String permission : group.getPermissions()) {
            if (StringUtils.startsWith(permission, "!") && permissions.contains(StringUtils.substringAfter(permission, "!"))) {
                continue;
            }
            
            permissions.add(permission);
        }
        
        return permissions;
    }
    
    public static String getUsername(User user) {
        return user.getName() + "#" + user.getDiscriminator() + " (" + user.getId() + ")";
    }
    
    public static Optional<Group> getGroup(Role role) {
        Server server = getServer(role.getGuild()).orElse(null);
        if (server == null || server.getGroups().isEmpty()) {
            return Optional.empty();
        }
        
        for (Group group : server.getGroups()) {
            if (group.getId() == role.getIdLong()) {
                return Optional.of(group);
            }
        }
        
        return Optional.empty();
    }
    
    public static Set<Group> getGroups(Member member) {
        Set<Group> groups = Toolbox.newLinkedHashSet();
        for (Role role : member.getGuild().getRoles()) {
            if (!groups.isEmpty() || member.getRoles().contains(role) || member.getGuild().getPublicRole() == role) {
                getGroup(role).ifPresent(groups::add);
            }
        }
        
        return groups;
    }
    
    public static Optional<Server> getServer(Guild guild) {
        Set<Server> servers = DiscordMusic.getInstance().getConfig().map(Config::getServers).orElse(null);
        if (servers == null || servers.isEmpty()) {
            return Optional.empty();
        }
        
        for (Server server : servers) {
            if (server.getId() == guild.getIdLong()) {
                return Optional.of(server);
            }
        }
        
        return Optional.empty();
    }
}