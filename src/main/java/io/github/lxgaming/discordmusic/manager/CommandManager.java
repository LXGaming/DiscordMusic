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
import io.github.lxgaming.discordmusic.command.ClearCommand;
import io.github.lxgaming.discordmusic.command.Command;
import io.github.lxgaming.discordmusic.command.DebugCommand;
import io.github.lxgaming.discordmusic.command.HelpCommand;
import io.github.lxgaming.discordmusic.command.InfoCommand;
import io.github.lxgaming.discordmusic.command.JoinCommand;
import io.github.lxgaming.discordmusic.command.PermissionCommand;
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
import io.github.lxgaming.discordmusic.configuration.category.GeneralCategory;
import io.github.lxgaming.discordmusic.configuration.category.MessageCategory;
import io.github.lxgaming.discordmusic.data.Color;
import io.github.lxgaming.discordmusic.exception.CommandException;
import io.github.lxgaming.discordmusic.util.StringUtils;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.Set;

public final class CommandManager {
    
    public static final Set<Command> COMMANDS = Toolbox.newLinkedHashSet();
    private static final Set<Class<? extends Command>> COMMAND_CLASSES = Toolbox.newHashSet();
    
    public static void prepare() {
        registerCommand(ClearCommand.class);
        registerCommand(DebugCommand.class);
        registerCommand(HelpCommand.class);
        registerCommand(InfoCommand.class);
        registerCommand(JoinCommand.class);
        registerCommand(PermissionCommand.class);
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
    
    public static boolean execute(Message message) {
        String content = parseMessage(MessageManager.getMessageContent(message));
        if (StringUtils.isBlank(content)) {
            return false;
        }
        
        List<String> arguments = getArguments(content);
        if (arguments == null || arguments.isEmpty()) {
            return false;
        }
        
        Command command = getCommand(arguments);
        if (command == null) {
            return false;
        }
        
        if (DiscordMusic.getInstance().getConfig().map(Config::getMessageCategory).map(MessageCategory::isDeleteInvoking).orElse(false)) {
            MessageManager.MESSAGES.add(message);
        }
        
        if (DiscordMusic.getInstance().getConfig().map(Config::getMessageCategory).map(MessageCategory::isSendTyping).orElse(false)) {
            message.getChannel().sendTyping().queue();
        }
        
        if (StringUtils.isBlank(command.getPermission()) || !PermissionManager.hasPermission(message.getMember(), command.getPermission())) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("You do not have permission to execute this command");
            embedBuilder.setFooter("Missing permission: " + command.getPermission(), "https://repo.lxgaming.me/assets/discord/warning.png");
            MessageManager.sendTemporaryMessage(message.getTextChannel(), embedBuilder.build());
            return false;
        }
        
        DiscordMusic.getInstance().getLogger().debug("Processing {} for {}#{} ({})",
                content,
                message.getAuthor().getName(), message.getAuthor().getDiscriminator(), message.getAuthor().getIdLong());
        
        try {
            command.execute(message, arguments);
            return true;
        } catch (CommandException ex) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("An error has occurred.");
            embedBuilder.getDescriptionBuilder()
                    .append("```")
                    .append(ex.getMessage())
                    .append("```");
            embedBuilder.setFooter("Exception: " + ex.getClass().getName(), "https://repo.lxgaming.me/assets/discord/error.png");
            MessageManager.sendMessage(message.getTextChannel(), embedBuilder.build());
            return false;
        } catch (Exception ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error while executing {}", Toolbox.getClassSimpleName(command.getClass()), ex);
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("An unexpected error has occurred. Details are available in console.");
            embedBuilder.setFooter("Exception: " + ex.getClass().getName(), "https://repo.lxgaming.me/assets/discord/error.png");
            MessageManager.sendMessage(message.getTextChannel(), embedBuilder.build());
            return false;
        }
    }
    
    public static boolean registerAlias(Command command, String alias) {
        if (StringUtils.containsIgnoreCase(command.getAliases(), alias)) {
            DiscordMusic.getInstance().getLogger().warn("{} is already registered for {}", alias, Toolbox.getClassSimpleName(command.getClass()));
            return false;
        }
        
        command.getAliases().add(alias);
        DiscordMusic.getInstance().getLogger().debug("{} registered for {}", alias, Toolbox.getClassSimpleName(command.getClass()));
        return true;
    }
    
    public static boolean registerCommand(Class<? extends Command> commandClass) {
        Command command = registerCommand(COMMANDS, commandClass);
        if (command != null) {
            DiscordMusic.getInstance().getLogger().debug("{} registered", Toolbox.getClassSimpleName(commandClass));
            return true;
        }
        
        return false;
    }
    
    public static boolean registerCommand(Command parentCommand, Class<? extends Command> commandClass) {
        if (parentCommand.getClass() == commandClass) {
            DiscordMusic.getInstance().getLogger().warn("{} attempted to register itself", Toolbox.getClassSimpleName(parentCommand.getClass()));
            return false;
        }
        
        Command command = registerCommand(parentCommand.getChildren(), commandClass);
        if (command != null) {
            command.parentCommand(parentCommand);
            DiscordMusic.getInstance().getLogger().debug("{} registered for {}", Toolbox.getClassSimpleName(commandClass), Toolbox.getClassSimpleName(parentCommand.getClass()));
            return true;
        }
        
        return false;
    }
    
    private static Command registerCommand(Set<Command> commands, Class<? extends Command> commandClass) {
        if (COMMAND_CLASSES.contains(commandClass)) {
            DiscordMusic.getInstance().getLogger().warn("{} is already registered", Toolbox.getClassSimpleName(commandClass));
            return null;
        }
        
        COMMAND_CLASSES.add(commandClass);
        Command command = Toolbox.newInstance(commandClass);
        if (command == null) {
            DiscordMusic.getInstance().getLogger().error("{} failed to initialize", Toolbox.getClassSimpleName(commandClass));
            return null;
        }
        
        if (!command.prepare()) {
            DiscordMusic.getInstance().getLogger().warn("{} failed to prepare", Toolbox.getClassSimpleName(commandClass));
            return null;
        }
        
        if (commands.add(command)) {
            return command;
        }
        
        return null;
    }
    
    public static Command getCommand(Class<? extends Command> commandClass) {
        return getCommand(null, commandClass);
    }
    
    public static Command getCommand(Command parentCommand, Class<? extends Command> commandClass) {
        Set<Command> commands = Toolbox.newLinkedHashSet();
        if (parentCommand != null) {
            commands.addAll(parentCommand.getChildren());
        } else {
            commands.addAll(COMMANDS);
        }
        
        for (Command command : commands) {
            if (command.getClass() == commandClass) {
                return command;
            }
            
            Command childCommand = getCommand(command, commandClass);
            if (childCommand != null) {
                return childCommand;
            }
        }
        
        return null;
    }
    
    public static Command getCommand(List<String> arguments) {
        return getCommand(null, arguments);
    }
    
    private static Command getCommand(Command parentCommand, List<String> arguments) {
        if (arguments.isEmpty()) {
            return parentCommand;
        }
        
        Set<Command> commands = Toolbox.newLinkedHashSet();
        if (parentCommand != null) {
            commands.addAll(parentCommand.getChildren());
        } else {
            commands.addAll(COMMANDS);
        }
        
        for (Command command : commands) {
            if (StringUtils.containsIgnoreCase(command.getAliases(), arguments.get(0))) {
                arguments.remove(0);
                return getCommand(command, arguments);
            }
        }
        
        return parentCommand;
    }
    
    private static List<String> getArguments(String string) {
        return Toolbox.newArrayList(StringUtils.split(string, " "));
    }
    
    private static String parseMessage(String message) {
        String commandPrefix = DiscordMusic.getInstance().getConfig()
                .map(Config::getGeneralCategory)
                .map(GeneralCategory::getCommandPrefix)
                .orElse(null);
        if (StringUtils.startsWithIgnoreCase(message, commandPrefix)) {
            return StringUtils.stripStart(StringUtils.removeStartIgnoreCase(message, commandPrefix), null);
        }
        
        if (StringUtils.startsWithIgnoreCase(message, "/")) {
            return StringUtils.stripStart(StringUtils.removeStartIgnoreCase(message, "/"), null);
        }
        
        return null;
    }
}