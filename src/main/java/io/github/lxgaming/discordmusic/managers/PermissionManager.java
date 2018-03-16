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
import io.github.lxgaming.discordmusic.util.DiscordUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.Set;

public class PermissionManager {
    
    public static void registerServer(Guild guild) {
        Set<Group> groups = DiscordUtil.newLinkedHashSet();
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
        
        return DiscordUtil.containsIgnoreCase(permissions, permission) || DiscordUtil.containsIgnoreCase(permissions, "*");
    }
    
    public static Set<String> getPermissions(Member member) {
        Set<String> permissions = DiscordUtil.newLinkedHashSet();
        for (Group group : getGroups(member)) {
            permissions.addAll(getPermissions(group));
        }
        
        return permissions;
    }
    
    public static Set<String> getPermissions(Group group) {
        Set<String> permissions = DiscordUtil.newLinkedHashSet();
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
    
    public static Set<Group> getGroups(Member member) {
        Set<Long> roles = DiscordUtil.newLinkedHashSet();
        for (Role role : member.getRoles()) {
            roles.add(role.getIdLong());
        }
        
        roles.add(member.getGuild().getPublicRole().getIdLong());
        
        Set<Group> groups = DiscordUtil.newLinkedHashSet();
        Optional<Server> server = getServer(member.getGuild());
        if (!server.isPresent()) {
            return groups;
        }
        
        for (Group group : server.get().getGroups()) {
            if (!groups.isEmpty() || roles.contains(group.getId())) {
                groups.add(group);
            }
        }
        
        return groups;
    }
    
    public static Optional<Group> getGroup(Role role) {
        Optional<Server> server = getServer(role.getGuild());
        if (!server.isPresent()) {
            return Optional.empty();
        }
        
        for (Group group : server.get().getGroups()) {
            if (group.getId() == role.getIdLong()) {
                return Optional.of(group);
            }
        }
        
        return Optional.empty();
    }
    
    public static Optional<Server> getServer(Guild guild) {
        Optional<Set<Server>> servers = DiscordMusic.getInstance().getConfig().map(Config::getServers);
        if (!servers.isPresent()) {
            return Optional.empty();
        }
        
        for (Server server : servers.get()) {
            if (server.getId() == guild.getIdLong()) {
                return Optional.of(server);
            }
        }
        
        return Optional.empty();
    }
}