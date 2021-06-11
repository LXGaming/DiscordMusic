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

package io.github.lxgaming.discordmusic.command.permission;

import io.github.lxgaming.discordmusic.command.Command;
import io.github.lxgaming.discordmusic.configuration.category.guild.RoleCategory;
import io.github.lxgaming.discordmusic.configuration.category.guild.UserCategory;
import io.github.lxgaming.discordmusic.entity.Color;
import io.github.lxgaming.discordmusic.manager.DiscordManager;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListPermissionCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("list");
        addAlias("l");
        description("List permissions");
        permission("permission.list");
        usage("[@Role | @User]");
        return true;
    }
    
    @Override
    public void execute(Message message, List<String> arguments) throws Exception {
        List<Member> members = message.getMentionedMembers(message.getGuild());
        List<Role> roles = message.getMentionedRoles();
        
        if ((!members.isEmpty() || !roles.isEmpty()) && !DiscordManager.hasPermission(message.getMember(), "permission.list.others")) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("You do not have permission to execute this command");
            embedBuilder.setFooter("Missing permission: permission.list.others", null);
            MessageManager.sendTemporaryMessage(message.getTextChannel(), embedBuilder.build());
            return;
        }
        
        if (!roles.isEmpty()) {
            Role role = roles.get(0);
            RoleCategory roleCategory = DiscordManager.getRoleCategory(role);
            execute(message.getChannel(), Collections.singleton(roleCategory), null);
        } else if (!members.isEmpty()) {
            Member member = members.get(0);
            UserCategory userCategory = DiscordManager.getUserCategory(member);
            execute(message.getChannel(), null, userCategory);
        } else {
            Set<RoleCategory> roleCategories;
            UserCategory userCategory;
            if (message.getMember() != null) {
                roleCategories = DiscordManager.getRoleCategories(message.getMember());
                userCategory = DiscordManager.getUserCategory(message.getMember());
            } else {
                roleCategories = null;
                userCategory = null;
            }
            
            execute(message.getChannel(), roleCategories, userCategory);
        }
    }
    
    private void execute(MessageChannel channel, Iterable<RoleCategory> roleCategories, UserCategory userCategory) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (roleCategories != null) {
            for (RoleCategory roleCategory : roleCategories) {
                if (roleCategory == null || roleCategory.getPermissions().isEmpty()) {
                    continue;
                }
                
                embedBuilder.addField(roleCategory.getName(), join(roleCategory.getPermissions()), false);
            }
        }
        
        if (userCategory != null && !userCategory.getPermissions().isEmpty()) {
            embedBuilder.addField(userCategory.getName(), join(userCategory.getPermissions()), false);
        }
        
        if (!embedBuilder.getFields().isEmpty()) {
            embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
            embedBuilder.setTitle("Permissions");
        } else {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("No permissions");
        }
        
        MessageManager.sendTemporaryMessage(channel, embedBuilder.build());
    }
    
    private String join(Map<String, Boolean> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : map.entrySet()) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append("\n");
            }
            
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue());
        }
        
        return stringBuilder.toString();
    }
}