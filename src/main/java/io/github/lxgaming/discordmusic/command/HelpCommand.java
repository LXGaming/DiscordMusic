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

import io.github.lxgaming.discordmusic.data.Color;
import io.github.lxgaming.discordmusic.manager.CommandManager;
import io.github.lxgaming.discordmusic.manager.GroupManager;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HelpCommand extends AbstractCommand {
    
    public HelpCommand() {
        addAlias("help");
        setDescription("Displays helpful information.");
        setPermission("command.help");
        setUsage("[Command]");
    }
    
    @Override
    public void execute(Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(MessageManager.getColor(Color.DEFAULT));
        
        if (arguments.isEmpty()) {
            for (AbstractCommand command : CommandManager.getCommands()) {
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
            embedBuilder.setFooter("<> = Required Argument, [] = Optional Argument", null);
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        Optional<AbstractCommand> command = CommandManager.getCommand(Toolbox.newArrayList(arguments.toArray(new String[0])));
        if (!command.isPresent()) {
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("No help present for " + StringUtils.join(arguments, " "));
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        if (!GroupManager.hasPermission(message.getMember(), command.get().getPermission())) {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("You do not have permission to view this command");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        embedBuilder.setTitle("Help: " + StringUtils.capitalize(command.get().getPrimaryAlias().orElse("Unknown")));
        if (command.get().getAliases().size() > 1) {
            embedBuilder.getDescriptionBuilder().append("**Aliases**: ").append(command.get().getAliases().stream().skip(1).collect(Collectors.joining(", ")));
            embedBuilder.getDescriptionBuilder().append("\n");
        }
        
        List<String> children = Toolbox.newArrayList();
        for (AbstractCommand childCommand : command.get().getChildren()) {
            children.add(childCommand.getPrimaryAlias().orElse("Unknown"));
        }
        
        if (!children.isEmpty()) {
            embedBuilder.getDescriptionBuilder().append("**Children**: ").append(StringUtils.join(children, ", "));
            embedBuilder.getDescriptionBuilder().append("\n");
        }
        
        embedBuilder.getDescriptionBuilder().append("**Description**: ");
        embedBuilder.getDescriptionBuilder().append(StringUtils.defaultIfBlank(command.get().getDescription(), "No description provided.")).append("\n");
        embedBuilder.getDescriptionBuilder().append("**Usage**: ").append(StringUtils.join(arguments, " "));
        if (StringUtils.isNotBlank(command.get().getUsage())) {
            embedBuilder.getDescriptionBuilder().append(" ").append(command.get().getUsage());
        }
        
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
}