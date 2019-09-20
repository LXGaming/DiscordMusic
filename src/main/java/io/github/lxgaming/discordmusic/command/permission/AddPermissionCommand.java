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

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.command.AbstractCommand;
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

public class AddPermissionCommand extends AbstractCommand {
    
    public AddPermissionCommand() {
        addAlias("add");
        addAlias("+");
        addAlias("grant");
        setDescription("Add permission");
        setPermission("permission.add");
        setUsage("<Permission> [@User | @Role]");
    }
    
    @Override
    public void execute(Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (arguments.isEmpty()) {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("Invalid Arguments: " + getUsage());
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        List<Member> members = message.getMentionedMembers(message.getGuild());
        List<Role> roles = message.getMentionedRoles();
        
        String permission = arguments.remove(0);
        if (!roles.isEmpty()) {
            Role role = roles.get(0);
            RoleCategory roleCategory = PermissionManager.getRoleCategory(role).orElse(null);
            if (roleCategory == null) {
                embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
                embedBuilder.setTitle("Failed to get RoleCategory for " + role.getName());
                MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
                return;
            }
            
            if (roleCategory.getPermissions().add(permission)) {
                DiscordMusic.getInstance().getConfiguration().saveConfiguration();
                embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
                embedBuilder.setTitle("Granted " + permission + " for " + role.getName());
            } else {
                embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
                embedBuilder.setTitle(permission + " is set for " + role.getName());
            }
            
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
        } else if (!members.isEmpty()) {
            Member member = members.get(0);
            UserCategory userCategory = PermissionManager.getOrCreateUserCategory(member).orElse(null);
            if (userCategory == null) {
                embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
                embedBuilder.setTitle("Failed to get UserCategory for " + member.getEffectiveName());
                MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
                return;
            }
            
            if (userCategory.getPermissions().add(permission)) {
                DiscordMusic.getInstance().getConfiguration().saveConfiguration();
                embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
                embedBuilder.setTitle("Granted " + permission + " for " + member.getEffectiveName());
            } else {
                embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
                embedBuilder.setTitle(permission + " is set for " + member.getEffectiveName());
            }
            
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
        } else {
            UserCategory userCategory = PermissionManager.getOrCreateUserCategory(message.getMember()).orElse(null);
            if (userCategory == null) {
                embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
                embedBuilder.setTitle("Failed to get UserCategory for " + message.getMember().getEffectiveName());
                MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
                return;
            }
            
            if (userCategory.getPermissions().add(permission)) {
                DiscordMusic.getInstance().getConfiguration().saveConfiguration();
                embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
                embedBuilder.setTitle("Granted " + permission + " for " + message.getMember().getEffectiveName());
            } else {
                embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
                embedBuilder.setTitle(permission + " is set for " + message.getMember().getEffectiveName());
            }
            
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
        }
    }
}