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

package io.github.lxgaming.discordmusic.managers;

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.commands.AbstractCommand;
import io.github.lxgaming.discordmusic.commands.ClearCommand;
import io.github.lxgaming.discordmusic.commands.DebugCommand;
import io.github.lxgaming.discordmusic.commands.HelpCommand;
import io.github.lxgaming.discordmusic.commands.InfoCommand;
import io.github.lxgaming.discordmusic.commands.JoinCommand;
import io.github.lxgaming.discordmusic.commands.PlayCommand;
import io.github.lxgaming.discordmusic.commands.PlayingCommand;
import io.github.lxgaming.discordmusic.commands.QueueCommand;
import io.github.lxgaming.discordmusic.commands.SearchCommand;
import io.github.lxgaming.discordmusic.commands.SelectCommand;
import io.github.lxgaming.discordmusic.commands.ShutdownCommand;
import io.github.lxgaming.discordmusic.commands.SkipCommand;
import io.github.lxgaming.discordmusic.commands.SourcesCommand;
import io.github.lxgaming.discordmusic.commands.StopCommand;
import io.github.lxgaming.discordmusic.commands.VolumeCommand;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class CommandManager {
    
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
    
    public static boolean process(TextChannel textChannel, Member member, Message message) {
        Optional<List<String>> arguments = getArguments(message.getContentDisplay()).map(Toolbox::newArrayList);
        if (!arguments.isPresent() || arguments.get().isEmpty()) {
            return false;
        }
        
        Optional<AbstractCommand> command = getCommand(arguments.get());
        if (!command.isPresent()) {
            return false;
        }
        
        if (DiscordMusic.getInstance().getConfig().map(Config::isDeleteInvoking).orElse(false)) {
            MessageManager.getMessages().add(message);
        }
        
        if (StringUtils.isBlank(command.get().getPermission()) || !GroupManager.hasPermission(member, command.get().getPermission())) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Toolbox.ERROR);
            embedBuilder.setTitle("You do not have permission to execute this command");
            embedBuilder.setFooter("Missing permission: " + command.get().getPermission(), null);
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return false;
        }
        
        DiscordMusic.getInstance().getLogger().debug("Processing {} for {}", command.get().getPrimaryAlias().orElse("Unknown"), GroupManager.getUsername(member.getUser()));
        command.get().execute(textChannel, member, message, arguments.get());
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