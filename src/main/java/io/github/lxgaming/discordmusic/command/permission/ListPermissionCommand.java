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
import io.github.lxgaming.discordmusic.configuration.category.RoleCategory;
import io.github.lxgaming.discordmusic.configuration.category.UserCategory;
import io.github.lxgaming.discordmusic.data.Color;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.manager.PermissionManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.Set;

public class ListPermissionCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("list");
        description("List permission");
        permission("permission.list");
        usage("[@User | @Role]");
        return true;
    }
    
    @Override
    public void execute(Message message, List<String> arguments) throws Exception {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        
        List<Member> members = message.getMentionedMembers(message.getGuild());
        List<Role> roles = message.getMentionedRoles();
        
        if ((!members.isEmpty() || !roles.isEmpty()) && !PermissionManager.hasPermission(message.getMember(), "permission.list.others")) {
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("You do not have permission to execute this command");
            embedBuilder.setFooter("Missing permission: permission.list.others", null);
            MessageManager.sendTemporaryMessage(message.getTextChannel(), embedBuilder.build());
            return;
        }
        
        Set<String> permissions;
        if (!roles.isEmpty()) {
            Role role = roles.get(0);
            RoleCategory roleCategory = PermissionManager.getRoleCategory(role).orElse(null);
            if (roleCategory == null) {
                embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
                embedBuilder.setTitle("Failed to get RoleCategory for " + role.getName());
                MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
                return;
            }
            
            permissions = roleCategory.getPermissions();
        } else if (!members.isEmpty()) {
            Member member = members.get(0);
            UserCategory userCategory = PermissionManager.getUserCategory(member).orElse(null);
            if (userCategory == null) {
                embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
                embedBuilder.setTitle("Failed to get UserCategory for " + member.getEffectiveName());
                MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
                return;
            }
            
            permissions = userCategory.getPermissions();
        } else {
            permissions = PermissionManager.getPermissions(message.getMember());
        }
        
        if (permissions.isEmpty()) {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("No permissions");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
        embedBuilder.setTitle("Permissions");
        for (String permission : permissions) {
            if (embedBuilder.getDescriptionBuilder().length() != 0) {
                embedBuilder.getDescriptionBuilder().append("\n");
            }
            
            embedBuilder.getDescriptionBuilder().append(permission);
        }
        
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
}