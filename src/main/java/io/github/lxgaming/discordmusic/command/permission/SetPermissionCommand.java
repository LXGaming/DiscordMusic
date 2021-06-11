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
import org.apache.commons.lang3.BooleanUtils;

import java.util.List;

public class SetPermissionCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("set");
        addAlias("add");
        addAlias("grant");
        description("Set permission");
        permission("permission.set");
        usage("<Permission> [Value] [@Role | @User]");
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
        boolean value;
        if (!arguments.isEmpty()) {
            value = BooleanUtils.toBoolean(arguments.remove(0));
        } else {
            value = true;
        }
        
        if (!roles.isEmpty()) {
            Role role = roles.get(0);
            execute(message.getChannel(), role, permission, value);
        } else if (!members.isEmpty()) {
            Member member = members.get(0);
            execute(message.getChannel(), member, permission, value);
        } else {
            execute(message.getChannel(), message.getMember(), permission, value);
        }
    }
    
    private void execute(MessageChannel channel, Role role, String permission, boolean value) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        RoleCategory roleCategory = DiscordManager.getOrCreateRoleCategory(role);
        if (roleCategory == null) {
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("Failed to get RoleCategory for " + role.getName());
            MessageManager.sendTemporaryMessage(channel, embedBuilder.build());
            return;
        }
        
        if (roleCategory.getPermissions().put(permission, value) != Boolean.valueOf(value)) {
            DiscordMusic.getInstance().getConfiguration().saveConfiguration();
        }
        
        if (value) {
            embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
            embedBuilder.setTitle("Set " + permission + " to true for " + role.getName());
        } else {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("Set " + permission + " to false for " + role.getName());
        }
        
        MessageManager.sendTemporaryMessage(channel, embedBuilder.build());
    }
    
    private void execute(MessageChannel channel, Member member, String permission, boolean value) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        UserCategory userCategory = DiscordManager.getOrCreateUserCategory(member);
        if (userCategory == null) {
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("Failed to get UserCategory for " + member.getUser().getAsTag());
            MessageManager.sendTemporaryMessage(channel, embedBuilder.build());
            return;
        }
        
        if (userCategory.getPermissions().put(permission, value) != Boolean.valueOf(value)) {
            DiscordMusic.getInstance().getConfiguration().saveConfiguration();
        }
        
        if (value) {
            embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
            embedBuilder.setTitle("Set " + permission + " to true for " + member.getUser().getAsTag());
        } else {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("Set " + permission + " to false for " + member.getUser().getAsTag());
        }
        
        MessageManager.sendTemporaryMessage(channel, embedBuilder.build());
    }
}