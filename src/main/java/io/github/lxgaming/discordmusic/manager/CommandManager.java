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

package io.github.lxgaming.discordmusic.manager;

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.command.AbstractCommand;
import io.github.lxgaming.discordmusic.command.ClearCommand;
import io.github.lxgaming.discordmusic.command.DebugCommand;
import io.github.lxgaming.discordmusic.command.HelpCommand;
import io.github.lxgaming.discordmusic.command.InfoCommand;
import io.github.lxgaming.discordmusic.command.JoinCommand;
import io.github.lxgaming.discordmusic.command.PlayCommand;
import io.github.lxgaming.discordmusic.command.PlayingCommand;
import io.github.lxgaming.discordmusic.command.QueueCommand;
import io.github.lxgaming.discordmusic.command.SearchCommand;
import io.github.lxgaming.discordmusic.command.SelectCommand;
import io.github.lxgaming.discordmusic.command.ShutdownCommand;
import io.github.lxgaming.discordmusic.command.SkipCommand;
import io.github.lxgaming.discordmusic.command.SourcesCommand;
import io.github.lxgaming.discordmusic.command.StopCommand;
import io.github.lxgaming.discordmusic.command.VolumeCommand;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.data.Color;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CommandManager {
    
    private static final Set<AbstractCommand> COMMANDS = Toolbox.newLinkedHashSet();
    private static final Set<Class<? extends AbstractCommand>> COMMAND_CLASSES = Toolbox.newLinkedHashSet();
    
    public static void buildCommands() {
        registerCommand(ClearCommand.class);
        registerCommand(DebugCommand.class);
        registerCommand(HelpCommand.class);
        registerCommand(InfoCommand.class);
        registerCommand(JoinCommand.class);
        registerCommand(PlayCommand.class);
        registerCommand(PlayingCommand.class);
        registerCommand(QueueCommand.class);
        registerCommand(SearchCommand.class);
        registerCommand(SelectCommand.class);
        registerCommand(ShutdownCommand.class);
        registerCommand(SkipCommand.class);
        registerCommand(SourcesCommand.class);
        registerCommand(StopCommand.class);
        registerCommand(VolumeCommand.class);
    }
    
    public static boolean process(Message message) {
        List<String> arguments = getArguments(MessageManager.getMessageContent(message)).map(Toolbox::newArrayList).orElse(null);
        if (arguments == null || arguments.isEmpty()) {
            return false;
        }
        
        Optional<AbstractCommand> command = getCommand(arguments);
        if (!command.isPresent()) {
            return false;
        }
        
        if (DiscordMusic.getInstance().getConfig().map(Config::isDeleteInvoking).orElse(false)) {
            MessageManager.getMessages().add(message);
        }
        
        if (StringUtils.isBlank(command.get().getPermission()) || !GroupManager.hasPermission(message.getMember(), command.get().getPermission())) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("You do not have permission to execute this command");
            embedBuilder.setFooter("Missing permission: " + command.get().getPermission(), null);
            MessageManager.sendTemporaryMessage(message.getTextChannel(), embedBuilder.build());
            return false;
        }
        
        DiscordMusic.getInstance().getLogger().debug("Processing {} for {}", command.get().getPrimaryAlias().orElse("Unknown"), GroupManager.getUsername(message.getMember().getUser()));
        
        try {
            command.get().execute(message, arguments);
        } catch (Exception ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error processing {}::process", "CommandManager", ex);
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("An error has occurred. Details are available in console.");
            embedBuilder.appendDescription(Toolbox.filter(MessageManager.getMessageContent(message)));
            embedBuilder.setFooter("Exception: " + ex.getClass().getSimpleName(), null);
            MessageManager.sendMessage(message.getTextChannel(), embedBuilder.build());
        }
        
        return true;
    }
    
    public static boolean registerCommand(Class<? extends AbstractCommand> commandClass) {
        if (getCommandClasses().contains(commandClass)) {
            DiscordMusic.getInstance().getLogger().warn("{} has already been registered", commandClass.getSimpleName());
            return false;
        }
        
        getCommandClasses().add(commandClass);
        Optional<AbstractCommand> command = Toolbox.newInstance(commandClass);
        if (!command.isPresent()) {
            DiscordMusic.getInstance().getLogger().error("{} failed to initialize", commandClass.getSimpleName());
            return false;
        }
        
        getCommands().add(command.get());
        DiscordMusic.getInstance().getLogger().debug("{} registered", commandClass.getSimpleName());
        return true;
    }
    
    public static boolean registerAlias(AbstractCommand command, String alias) {
        if (Toolbox.containsIgnoreCase(command.getAliases(), alias)) {
            DiscordMusic.getInstance().getLogger().warn("{} has already been registered for {}", alias, command.getClass().getSimpleName());
            return false;
        }
        
        command.getAliases().add(alias);
        DiscordMusic.getInstance().getLogger().debug("{} registered for {}", alias, command.getClass().getSimpleName());
        return true;
    }
    
    public static boolean registerCommand(AbstractCommand parentCommand, Class<? extends AbstractCommand> commandClass) {
        if (parentCommand.getClass() == commandClass) {
            DiscordMusic.getInstance().getLogger().warn("{} attempted to register itself", parentCommand.getClass().getSimpleName());
            return false;
        }
        
        if (getCommandClasses().contains(commandClass)) {
            DiscordMusic.getInstance().getLogger().warn("{} has already been registered", commandClass.getSimpleName());
            return false;
        }
        
        getCommandClasses().add(commandClass);
        Optional<AbstractCommand> command = Toolbox.newInstance(commandClass);
        if (!command.isPresent()) {
            DiscordMusic.getInstance().getLogger().error("{} failed to initialize", commandClass.getSimpleName());
            return false;
        }
        
        parentCommand.getChildren().add(command.get());
        DiscordMusic.getInstance().getLogger().debug("{} registered for {}", commandClass.getSimpleName(), parentCommand.getClass().getSimpleName());
        return true;
    }
    
    public static Optional<AbstractCommand> getCommand(List<String> arguments) {
        return getCommand(null, arguments);
    }
    
    private static Optional<AbstractCommand> getCommand(AbstractCommand parentCommand, List<String> arguments) {
        Set<AbstractCommand> commands = Toolbox.newLinkedHashSet();
        if (parentCommand != null) {
            commands.addAll(parentCommand.getChildren());
        } else {
            commands.addAll(getCommands());
        }
        
        if (arguments.isEmpty() || commands.isEmpty()) {
            return Optional.ofNullable(parentCommand);
        }
        
        for (AbstractCommand command : commands) {
            if (Toolbox.containsIgnoreCase(command.getAliases(), arguments.get(0))) {
                arguments.remove(0);
                return getCommand(command, arguments);
            }
        }
        
        return Optional.ofNullable(parentCommand);
    }
    
    private static Optional<String[]> getArguments(String message) {
        String commandPrefix = DiscordMusic.getInstance().getConfig().map(Config::getCommandPrefix).orElse("/");
        if (StringUtils.startsWithIgnoreCase(message, commandPrefix)) {
            return Optional.ofNullable(StringUtils.split(Toolbox.filter(StringUtils.removeStartIgnoreCase(message, commandPrefix)), " "));
        }
        
        if (StringUtils.startsWithIgnoreCase(message, "/")) {
            return Optional.ofNullable(StringUtils.split(Toolbox.filter(StringUtils.removeStartIgnoreCase(message, "/")), " "));
        }
        
        return Optional.empty();
    }
    
    public static Set<AbstractCommand> getCommands() {
        return COMMANDS;
    }
    
    private static Set<Class<? extends AbstractCommand>> getCommandClasses() {
        return COMMAND_CLASSES;
    }
}