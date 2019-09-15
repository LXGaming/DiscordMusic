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

package io.github.lxgaming.discordmusic.command;

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.category.GeneralCategory;
import io.github.lxgaming.discordmusic.data.Color;
import io.github.lxgaming.discordmusic.manager.CommandManager;
import io.github.lxgaming.discordmusic.manager.GroupManager;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends AbstractCommand {
    
    public HelpCommand() {
        addAlias("help");
        addAlias("?");
        setDescription("Displays helpful information.");
        setPermission("help.base");
        setUsage("[Command]");
    }
    
    @Override
    public void execute(Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(MessageManager.getColor(Color.DEFAULT));
        
        if (arguments.isEmpty()) {
            for (AbstractCommand command : CommandManager.COMMANDS) {
                if (command.getAliases().isEmpty() || !GroupManager.hasPermission(message.getMember(), command.getPermission())) {
                    continue;
                }
                
                if (embedBuilder.getDescriptionBuilder().length() != 0) {
                    embedBuilder.getDescriptionBuilder().append("\n");
                }
                
                embedBuilder.getDescriptionBuilder().append("**");
                embedBuilder.getDescriptionBuilder().append(StringUtils.capitalize(command.getPrimaryAlias().orElse("Unknown")));
                embedBuilder.getDescriptionBuilder().append("**: ");
                embedBuilder.getDescriptionBuilder().append(StringUtils.defaultIfBlank(command.getDescription(), "No description provided."));
            }
            
            embedBuilder.setTitle("Help: Index");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        List<String> childArguments = Toolbox.newArrayList(arguments.toArray(new String[0]));
        AbstractCommand command = CommandManager.getCommand(childArguments).orElse(null);
        if (command == null) {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("Unknown command");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        int index = (arguments.size() - childArguments.size());
        while (index < arguments.size()) {
            arguments.remove(index);
        }
        
        if (!GroupManager.hasPermission(message.getMember(), command.getPermission())) {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("You do not have permission to view this command");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        embedBuilder.setTitle("Help: " + StringUtils.capitalize(command.getPrimaryAlias().orElse("Unknown")));
        if (command.getAliases().size() > 1) {
            embedBuilder.getDescriptionBuilder().append("**Aliases**: ").append(command.getAliases().stream().skip(1).collect(Collectors.joining(", ")));
            embedBuilder.getDescriptionBuilder().append("\n");
        }
        
        List<String> children = Toolbox.newArrayList();
        for (AbstractCommand childCommand : command.getChildren()) {
            children.add(childCommand.getPrimaryAlias().orElse("Unknown"));
        }
        
        if (!children.isEmpty()) {
            embedBuilder.getDescriptionBuilder().append("**Children**: ").append(String.join(", ", children));
            embedBuilder.getDescriptionBuilder().append("\n");
        }
        
        embedBuilder.getDescriptionBuilder().append("**Description**: ");
        embedBuilder.getDescriptionBuilder().append(StringUtils.defaultIfBlank(command.getDescription(), "No description provided.")).append("\n");
        embedBuilder.getDescriptionBuilder().append("**Usage**: ")
                .append(DiscordMusic.getInstance().getConfig().map(Config::getGeneralCategory).map(GeneralCategory::getCommandPrefix).orElse("/"))
                .append(" ")
                .append(String.join(" ", arguments));
        
        if (StringUtils.isNotBlank(command.getUsage())) {
            embedBuilder.getDescriptionBuilder().append(" ").append(command.getUsage());
        }
        
        embedBuilder.setFooter("<> = Required Argument, [] = Optional Argument", null);
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
}