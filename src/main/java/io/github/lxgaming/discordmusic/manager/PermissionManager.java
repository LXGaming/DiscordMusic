/*
 * Copyright 2019 Alex Thomson
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

package io.github.lxgaming.discordmusic.manager;

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.category.GuildCategory;
import io.github.lxgaming.discordmusic.configuration.category.RoleCategory;
import io.github.lxgaming.discordmusic.configuration.category.UserCategory;
import io.github.lxgaming.discordmusic.util.StringUtils;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Optional;
import java.util.Set;

public final class PermissionManager {
    
    public static void register(Guild guild) {
        Set<RoleCategory> roleCategories = Toolbox.newLinkedHashSet();
        for (Role role : guild.getRoles()) {
            RoleCategory roleCategory = getRoleCategory(role).orElse(new RoleCategory());
            roleCategory.setId(role.getIdLong());
            roleCategory.setName(role.getName());
            roleCategories.add(roleCategory);
        }
        
        GuildCategory guildCategory = getGuildCategory(guild).orElse(new GuildCategory());
        guildCategory.setId(guild.getIdLong());
        guildCategory.setName(guild.getName());
        guildCategory.getRoleCategories().clear();
        guildCategory.getRoleCategories().addAll(roleCategories);
        
        DiscordMusic.getInstance().getConfig().map(Config::getGuildCategories).ifPresent(guildCategories -> guildCategories.add(guildCategory));
        DiscordMusic.getInstance().getConfiguration().saveConfiguration();
    }
    
    public static boolean hasPermission(Member member, String permission) {
        Set<String> permissions = getPermissions(member);
        if (permissions.isEmpty() || permissions.contains("!" + permission)) {
            return false;
        }
        
        return StringUtils.containsIgnoreCase(permissions, permission) || StringUtils.containsIgnoreCase(permissions, "*");
    }
    
    public static Set<String> getPermissions(Member member) {
        Set<String> permissions = Toolbox.newLinkedHashSet();
        getUserCategory(member).ifPresent(userCategory -> {
            userCategory.getPermissions().forEach(permission -> appendPermission(permissions, permission));
        });
        
        for (RoleCategory roleCategory : getRoleCategories(member)) {
            roleCategory.getPermissions().forEach(permission -> appendPermission(permissions, permission));
        }
        
        return permissions;
    }
    
    private static void appendPermission(Set<String> permissions, String permission) {
        if (permission.startsWith("!") && permissions.contains(permission.substring(1))) {
            return;
        }
        
        permissions.add(permission);
    }
    
    public static Set<RoleCategory> getRoleCategories(Member member) {
        Set<RoleCategory> roleCategories = Toolbox.newLinkedHashSet();
        for (Role role : member.getRoles()) {
            getRoleCategory(role).ifPresent(roleCategories::add);
        }
        
        getRoleCategory(member.getGuild().getPublicRole()).ifPresent(roleCategories::add);
        return roleCategories;
    }
    
    public static Optional<RoleCategory> getRoleCategory(Role role) {
        GuildCategory guildCategory = getGuildCategory(role.getGuild()).orElse(null);
        if (guildCategory == null) {
            return Optional.empty();
        }
        
        for (RoleCategory roleCategory : guildCategory.getRoleCategories()) {
            if (roleCategory.getId() == role.getIdLong()) {
                return Optional.of(roleCategory);
            }
        }
        
        return Optional.empty();
    }
    
    public static Optional<UserCategory> getOrCreateUserCategory(Member member) {
        GuildCategory guildCategory = getGuildCategory(member.getGuild()).orElse(null);
        if (guildCategory == null) {
            return Optional.empty();
        }
        
        for (UserCategory userCategory : guildCategory.getUserCategories()) {
            if (userCategory.getId() == member.getIdLong()) {
                return Optional.of(userCategory);
            }
        }
        
        UserCategory userCategory = new UserCategory();
        userCategory.setId(member.getIdLong());
        userCategory.setName(member.getUser().getName());
        guildCategory.getUserCategories().add(userCategory);
        return Optional.of(userCategory);
    }
    
    public static Optional<UserCategory> getUserCategory(Member member) {
        GuildCategory guildCategory = getGuildCategory(member.getGuild()).orElse(null);
        if (guildCategory == null) {
            return Optional.empty();
        }
        
        for (UserCategory userCategory : guildCategory.getUserCategories()) {
            if (userCategory.getId() == member.getIdLong()) {
                return Optional.of(userCategory);
            }
        }
        
        return Optional.empty();
    }
    
    public static Optional<GuildCategory> getGuildCategory(Guild guild) {
        Set<GuildCategory> guildCategories = DiscordMusic.getInstance().getConfig().map(Config::getGuildCategories).orElse(null);
        if (guildCategories == null) {
            return Optional.empty();
        }
        
        for (GuildCategory guildCategory : guildCategories) {
            if (guildCategory.getId() == guild.getIdLong()) {
                return Optional.of(guildCategory);
            }
        }
        
        return Optional.empty();
    }
}