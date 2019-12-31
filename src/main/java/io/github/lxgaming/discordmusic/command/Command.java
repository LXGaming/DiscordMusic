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

package io.github.lxgaming.discordmusic.command;

import io.github.lxgaming.discordmusic.manager.CommandManager;
import io.github.lxgaming.discordmusic.util.StringUtils;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class Command {
    
    private final Set<String> aliases = Toolbox.newLinkedHashSet();
    private final Set<Command> children = Toolbox.newLinkedHashSet();
    private Command parentCommand;
    private String description;
    private String permission;
    private String usage;
    
    public abstract boolean prepare();
    
    public abstract void execute(Message message, List<String> arguments) throws Exception;
    
    public final Optional<String> getPrimaryAlias() {
        for (String alias : aliases) {
            if (StringUtils.isNotBlank(alias)) {
                return Optional.of(alias);
            }
        }
        
        return Optional.empty();
    }
    
    public final List<String> getPath() {
        List<String> paths = Toolbox.newArrayList();
        if (parentCommand != null) {
            paths.addAll(parentCommand.getPath());
        }
        
        getPrimaryAlias().ifPresent(paths::add);
        return paths;
    }
    
    protected final void addAlias(String alias) {
        CommandManager.registerAlias(this, alias);
    }
    
    public final Set<String> getAliases() {
        return aliases;
    }
    
    protected final void addChild(Class<? extends Command> commandClass) {
        CommandManager.registerCommand(this, commandClass);
    }
    
    public final Set<Command> getChildren() {
        return children;
    }
    
    public final Command getParentCommand() {
        return parentCommand;
    }
    
    public final void parentCommand(Command parentCommand) {
        if (this.parentCommand != null) {
            throw new IllegalStateException("ParentCommand is already set");
        }
        
        this.parentCommand = parentCommand;
    }
    
    public final String getDescription() {
        return description;
    }
    
    protected final void description(String description) {
        if (this.description != null) {
            throw new IllegalStateException("Description is already set");
        }
        
        this.description = description;
    }
    
    public final String getPermission() {
        return permission;
    }
    
    protected final void permission(String permission) {
        if (this.permission != null) {
            throw new IllegalStateException("Permission is already set");
        }
        
        this.permission = permission;
    }
    
    public final String getUsage() {
        return usage;
    }
    
    protected final void usage(String usage) {
        if (this.usage != null) {
            throw new IllegalStateException("Usage is already set");
        }
        
        this.usage = usage;
    }
}