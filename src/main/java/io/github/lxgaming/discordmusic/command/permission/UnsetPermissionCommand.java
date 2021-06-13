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
import io.github.lxgaming.discordmusic.command.Command;
import io.github.lxgaming.discordmusic.command.HelpCommand;
import io.github.lxgaming.discordmusic.configuration.category.guild.RoleCategory;
import io.github.lxgaming.discordmusic.configuration.category.guild.UserCategory;
import io.github.lxgaming.discordmusic.entity.Color;
import io.github.lxgaming.discordmusic.manager.CommandManager;
import io.github.lxgaming.discordmusic.manager.DiscordManager;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class UnsetPermissionCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("unset");
        addAlias("remove");
        addAlias("revoke");
        description("Unset permission");
        permission("permission.unset");
        usage("<Permission> [@Role | @User]");
        return true;
    }
    
    @Override
    public void execute(Message message, List<String> arguments) throws Exception {
        if (arguments.isEmpty()) {
            Command command = CommandManager.getCommand(HelpCommand.class);
            if (command != null) {
                command.execute(message, getPath());
            }
            
            return;
        }
        
        List<Member> members = message.getMentionedMembers(message.getGuild());
        List<Role> roles = message.getMentionedRoles();
        
        String permission = arguments.remove(0);
        if (!roles.isEmpty()) {
            Role role = roles.get(0);
            execute(message.getChannel(), role, permission);
        } else if (!members.isEmpty()) {
            Member member = members.get(0);
            execute(message.getChannel(), member, permission);
        } else {
            execute(message.getChannel(), message.getMember(), permission);
        }
    }
    
    private void execute(MessageChannel channel, Role role, String permission) {
        RoleCategory roleCategory = DiscordManager.getOrCreateRoleCategory(role);
        if (roleCategory == null) {
            EmbedBuilder embedBuilder = MessageManager.createErrorEmbed("Failed to get RoleCategory for " + role.getName());
            MessageManager.sendTemporaryMessage(channel, embedBuilder.build());
            return;
        }
        
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (roleCategory.getPermissions().remove(permission) != null) {
            DiscordMusic.getInstance().getConfiguration().saveConfiguration();
            embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
            embedBuilder.setTitle("Unset " + permission + " for " + role.getName());
        } else {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle(permission + " is not set for " + role.getName());
        }
        
        MessageManager.sendTemporaryMessage(channel, embedBuilder.build());
    }
    
    private void execute(MessageChannel channel, Member member, String permission) {
        UserCategory userCategory = DiscordManager.getOrCreateUserCategory(member);
        if (userCategory == null) {
            EmbedBuilder embedBuilder = MessageManager.createErrorEmbed("Failed to get UserCategory for " + member.getUser().getAsTag());
            MessageManager.sendTemporaryMessage(channel, embedBuilder.build());
            return;
        }
        
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (userCategory.getPermissions().remove(permission) != null) {
            DiscordMusic.getInstance().getConfiguration().saveConfiguration();
            embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
            embedBuilder.setTitle("Unset " + permission + " for " + member.getUser().getAsTag());
        } else {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle(permission + " is not set for " + member.getUser().getAsTag());
        }
        
        MessageManager.sendTemporaryMessage(channel, embedBuilder.build());
    }
}