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

import com.google.common.collect.Sets;
import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.category.AccountCategory;
import io.github.lxgaming.discordmusic.configuration.category.GuildCategory;
import io.github.lxgaming.discordmusic.configuration.category.guild.RoleCategory;
import io.github.lxgaming.discordmusic.configuration.category.guild.UserCategory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.Map;
import java.util.Set;

public final class DiscordManager {
    
    public static void register(Guild guild) {
        GuildCategory guildCategory = getOrCreateGuildCategory(guild);
        if (guildCategory == null) {
            return;
        }
        
        guildCategory.setId(guild.getIdLong());
        guildCategory.setName(guild.getName());
        
        Set<RoleCategory> roleCategories = Sets.newLinkedHashSet();
        for (Role role : guild.getRoles()) {
            RoleCategory roleCategory = getRoleCategory(role);
            if (roleCategory == null) {
                continue;
            }
            
            roleCategory.setId(role.getIdLong());
            roleCategory.setName(role.getName());
            if (role.isPublicRole()) {
                roleCategory.setInheritable(true);
            }
            
            roleCategories.add(roleCategory);
        }
        
        guildCategory.getRoleCategories().clear();
        guildCategory.getRoleCategories().addAll(roleCategories);
    }
    
    public static User getOwner() {
        long ownerId = DiscordMusic.getInstance().getConfig().map(Config::getAccountCategory).map(AccountCategory::getOwnerId).orElse(0L);
        return ownerId != 0L ? AccountManager.getJDA().getUserById(ownerId) : null;
    }
    
    public static boolean isOwner(User user) {
        long ownerId = DiscordMusic.getInstance().getConfig().map(Config::getAccountCategory).map(AccountCategory::getOwnerId).orElse(0L);
        return ownerId != 0L && ownerId == user.getIdLong();
    }
    
    public static boolean hasPermission(Member member, String permission) {
        if (isOwner(member.getUser())) {
            return true;
        }
        
        UserCategory userCategory = getUserCategory(member);
        if (userCategory == null) {
            return false;
        }
        
        if (userCategory.getPermissions() != null) {
            Boolean userPermission = hasPermission(userCategory.getPermissions(), permission);
            if (userPermission != null) {
                return userPermission;
            }
        }
        
        Set<RoleCategory> roleCategories = getRoleCategories(member);
        if (roleCategories != null) {
            Boolean rolePermission = hasPermission(roleCategories, permission);
            if (rolePermission != null) {
                return rolePermission;
            }
        }
        
        return false;
    }
    
    private static Boolean hasPermission(Iterable<RoleCategory> roleCategories, String permission) {
        for (RoleCategory roleCategory : roleCategories) {
            Boolean value = hasPermission(roleCategory.getPermissions(), permission);
            if (value != null) {
                return value;
            }
        }
        
        return null;
    }
    
    private static Boolean hasPermission(Map<String, Boolean> permissions, String permission) {
        Boolean value = permissions.get(permission);
        if (value != null) {
            return value;
        }
        
        return permissions.get("*");
    }
    
    public static Set<RoleCategory> getRoleCategories(Member member) {
        GuildCategory guildCategory = getGuildCategory(member.getGuild());
        if (guildCategory == null || guildCategory.getRoleCategories() == null || guildCategory.getRoleCategories().isEmpty()) {
            return null;
        }
        
        Set<RoleCategory> userRoleCategories = Sets.newLinkedHashSet();
        for (Role role : member.getGuild().getRoles()) {
            if (member.getRoles().contains(role) || role.isPublicRole()) {
                RoleCategory roleCategory = getRoleCategory(guildCategory.getRoleCategories(), role);
                if (roleCategory != null) {
                    userRoleCategories.add(roleCategory);
                }
                
                continue;
            }
            
            if (!userRoleCategories.isEmpty()) {
                RoleCategory roleCategory = getRoleCategory(guildCategory.getRoleCategories(), role);
                if (roleCategory != null && roleCategory.isInheritable()) {
                    userRoleCategories.add(roleCategory);
                }
            }
        }
        
        return userRoleCategories;
    }
    
    public static RoleCategory getOrCreateRoleCategory(Role role) {
        GuildCategory guildCategory = getGuildCategory(role.getGuild());
        if (guildCategory == null) {
            return null;
        }
        
        RoleCategory existingRoleCategory = getRoleCategory(guildCategory.getRoleCategories(), role);
        if (existingRoleCategory != null) {
            return existingRoleCategory;
        }
        
        RoleCategory roleCategory = new RoleCategory();
        roleCategory.setId(role.getIdLong());
        roleCategory.setName(role.getName());
        
        if (guildCategory.getRoleCategories().add(roleCategory)) {
            return roleCategory;
        }
        
        return null;
    }
    
    public static RoleCategory getRoleCategory(Role role) {
        GuildCategory guildCategory = getGuildCategory(role.getGuild());
        if (guildCategory == null) {
            return null;
        }
        
        return getRoleCategory(guildCategory.getRoleCategories(), role);
    }
    
    public static RoleCategory getRoleCategory(Iterable<RoleCategory> roleCategories, Role role) {
        for (RoleCategory roleCategory : roleCategories) {
            if (roleCategory.getId() == role.getIdLong()) {
                return roleCategory;
            }
        }
        
        return null;
    }
    
    public static UserCategory getOrCreateUserCategory(Member member) {
        GuildCategory guildCategory = getGuildCategory(member.getGuild());
        if (guildCategory == null) {
            return null;
        }
        
        UserCategory existingUserCategory = getUserCategory(guildCategory.getUserCategories(), member);
        if (existingUserCategory != null) {
            return existingUserCategory;
        }
        
        UserCategory userCategory = new UserCategory();
        userCategory.setId(member.getIdLong());
        userCategory.setName(member.getUser().getName());
        
        if (guildCategory.getUserCategories().add(userCategory)) {
            return userCategory;
        }
        
        return null;
    }
    
    public static UserCategory getUserCategory(Member member) {
        GuildCategory guildCategory = getGuildCategory(member.getGuild());
        if (guildCategory == null) {
            return null;
        }
        
        return getUserCategory(guildCategory.getUserCategories(), member);
    }
    
    private static UserCategory getUserCategory(Iterable<UserCategory> userCategories, Member member) {
        for (UserCategory userCategory : userCategories) {
            if (userCategory.getId() == member.getIdLong()) {
                return userCategory;
            }
        }
        
        return null;
    }
    
    public static GuildCategory getOrCreateGuildCategory(Guild guild) {
        Set<GuildCategory> guildCategories = DiscordMusic.getInstance().getConfig().map(Config::getGuildCategories).orElse(null);
        if (guildCategories == null) {
            return null;
        }
        
        GuildCategory existingGuildCategory = getGuildCategory(guildCategories, guild);
        if (existingGuildCategory != null) {
            return existingGuildCategory;
        }
        
        GuildCategory guildCategory = new GuildCategory();
        guildCategory.setId(guild.getIdLong());
        guildCategory.setName(guild.getName());
        
        if (guildCategories.add(guildCategory)) {
            return guildCategory;
        }
        
        return null;
    }
    
    public static GuildCategory getGuildCategory(Guild guild) {
        Set<GuildCategory> guildCategories = DiscordMusic.getInstance().getConfig().map(Config::getGuildCategories).orElse(null);
        if (guildCategories == null) {
            return null;
        }
        
        return getGuildCategory(guildCategories, guild);
    }
    
    private static GuildCategory getGuildCategory(Iterable<GuildCategory> guildCategories, Guild guild) {
        for (GuildCategory guildCategory : guildCategories) {
            if (guildCategory.getId() == guild.getIdLong()) {
                return guildCategory;
            }
        }
        
        return null;
    }
}